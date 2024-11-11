package com.qkinfotech.core.web.vue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

public class VueProcessor {

	PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	@Getter
	@Setter
	private static class Vue {

		private String etag;
		private String uri;
		private Resource resource;
		private String script = "";
		private int scriptLength = 0;
		private String style = "";
		private int styleLength = 0;
		private String template = "";

		public Vue(String uri, Resource resource) {
			this.uri = uri;
			this.resource = resource;
			try {
				etag = "" + resource.lastModified();
				parse();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void parse() throws IOException, ParserException {
			String text = resource.getContentAsString(Charset.defaultCharset());
			Parser parser = new Parser(text);
			String tag = null;
			int s = 0, e = 0;
			for (NodeIterator i = parser.elements(); i.hasMoreNodes();) {
				Node node = i.nextNode();
				if (tag == null && node.getText().startsWith("template")) {
					tag = "template";
					s = node.getEndPosition();
				}
				if ("template".equals(tag) && "/template".equals(node.getText())) {
					e = node.getStartPosition();
					template = text.substring(s, e);
					tag = null;
				}
				if (tag == null && node.getText().startsWith("script")) {
					script = node.toHtml();
					script = script.substring(node.getText().length() + 2);
					script = script.substring(0, script.length() - 9);
				}
				if (tag == null && node.getText().startsWith("style")) {
					style = node.toHtml();
					style = style.substring(node.getText().length() + 2);
					style = style.substring(0, style.length() - 8);
				}
			}

			if (StringUtils.hasText(style)) {
				String link = "let link = document.createElement('link');\r\n";
				link += "link.rel = 'stylesheet';\r\n";
				link += "link.href = '" + uri + ".css';\r\n";
				link += "document.head.appendChild(link);\r\n";
				script = link + script;
			}
			styleLength = style.getBytes(Charset.defaultCharset()).length;

			if (StringUtils.hasText(template)) {
				String t = StringEscapeUtils.unescapeEcmaScript(template);
				Matcher m = Pattern.compile("export\\s+default\\s*\\{").matcher(script);

				if(m.find()) {
					s = m.start();
					e = m.end();
					
					script = script.substring(0, s) + "export default {\r\n\ttemplate: `" + t + "`,\r\n" + script.substring(e) ;
				}
			}
			scriptLength = script.getBytes(Charset.defaultCharset()).length;
		}

	}

	private static Map<String, Vue> pages = new Hashtable<>();

	private Vue loadVue(HttpServletRequest request, HttpServletResponse response) throws NoResourceFoundException, IOException {
		String uri = request.getRequestURI();
		if(uri.endsWith(".css")) {
			uri = uri.substring(0, uri.length() - 4);
		}

		if (pages.containsKey(uri)) {
			Vue vue = pages.get(uri);
			if (!vue.getEtag().equals("" + vue.getResource().lastModified())) {
				pages.remove(uri);
			}
		}
		if (!pages.containsKey(uri)) {
			Resource[] resources = resolver.getResources("classpath:/static" + uri);
			if (resources.length != 1) {
				throw new NoResourceFoundException(HttpMethod.GET, uri);
			}

			pages.put(uri, new Vue(uri, resources[0]));
		}
		return pages.get(uri);

	}

	private String cacheControl(Vue vue, HttpServletRequest request, HttpServletResponse response) throws IOException {

		String eTag = "" + vue.getResource().lastModified();

		String match = request.getHeader("If-None-Match");
		if (eTag.equals(match)) {
			response.setStatus(304);
			return null;
		}

		return eTag;
	}

	public void vueCss(HttpServletRequest request, HttpServletResponse response) throws IOException, NoResourceFoundException {
		Vue vue = loadVue(request, response);
		String eTag = cacheControl(vue, request, response);

		if (eTag == null) {
			return;
		}

		response.addHeader("etag", eTag);
		response.setContentType("text/css");
		response.setCharacterEncoding("UTF-8");
		response.setContentLength(vue.getStyleLength());
		response.getWriter().print(vue.getStyle());

	}

	public void vueJs(HttpServletRequest request, HttpServletResponse response) throws IOException, NoResourceFoundException {
		Vue vue = loadVue(request, response);
		String eTag = cacheControl(vue, request, response);

		if (eTag == null) {
			return;
		}

		response.addHeader("etag", eTag);
		response.setContentType("text/javascript");
		response.setCharacterEncoding("UTF-8");
		response.setContentLength(vue.getScriptLength());
		response.getWriter().print(vue.getScript());

	}

}
