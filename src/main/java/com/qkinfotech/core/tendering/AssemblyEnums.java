package com.qkinfotech.core.tendering;

import lombok.Getter;

@Getter
public enum AssemblyEnums {
    //国招工作台-我的流程
    MY_PROCESS("UserProcess","/km/review/?j_iframe=true&j_aside=false#j_path=%2FlistAll&mydoc=all"
            ,"/km/review/km_review_main/kmReviewMainPortlet.do?method=listPortlet&myFlow=all&fdCategoryId=&rowsize=10&dataview=classic&scope=no&t=1721896797839&s_ajax=true"
            ,"jsonArray",null,"review-my"),
    //国招工作台-公众号流程
    ASSEMBLY_PROCESS("OfficialAccountProcess","/km/review/?j_iframe=true&j_aside=false#j_path=%2FlistAll&mydoc=all&cri.q=fdTemplate%3A178d98a0af3ede0b699e4684eb3b4db2"
            ,"/km/review/km_review_main/kmReviewMainPortlet.do?method=listPortlet&myFlow=all&fdCategoryId=178d98a0af3ede0b699e4684eb3b4db2&rowsize=6&dataview=classic&scope=no&t=1721956775451&s_ajax=true"
            ,"jsonArray",null,"off-account"),
    //国招工作台-采购公告
    PROCUREMENT_NOTICE("PurchaseNotice","/sys/news/?j_iframe=true&j_aside=false&categoryId=13f89f0aecc9570a269328c4ea8b510d#j_path=%2FdocCategory&docCategory=13f89f0aecc9570a269328c4ea8b510d&cri.q=docStatus%3A30"
            ,"/sys/common/dataxml.jsp?s_bean=sysNewsMainPortletService&cateid=13f89f0aecc9570a269328c4ea8b510d&rowsize=6&type=main&scope=no&t=1721956775450&s_ajax=true"
            ,"xml",new String[]{"text","catehref","publishTime","width","creator","id","height","importance","created","href","catename","fdAuthor"},"procurementMy"),
    //国招工作台-法律法规知识库
    LAW_KNOWLEDGE_BASE("LawKnowledge","/km/institution/?j_iframe=true&j_aside=false&categoryId=#cri.q=docStatus%3A30&j_path=%2FallDoc"
            ,"/sys/common/dataxml.jsp?s_bean=kmInstitutionKnowledgePortlet&fdCategoryId=&rowsize=10&scope=no&t=1721956775452&s_ajax=true"
            ,"xml",new String[]{"id","text","catehref","created","href","catename","creator"},"institution"),
    //国招工作台-线下待审
    OFFLINE_AUDIT("OfflineAudit","/km/project/km_project_sign_info/index.jsp?j_iframe=true&j_aside=false#cri.q=fdPayStatus:0"
                               ,"/km/project/km_project_sign_info/kmProjectSignInfoData.do?method=fdKmProject&rowsize=10&t=1721963164870&s_ajax=true"
                               ,"jsonArray",null,"sign-info"),
    //国招工作台-考试安排
    EXAM_ARRANGE("ExamArrange","/kms/exam/admin/?j_iframe=true&j_aside=false#j_path=%2Fall"
            ,"/kms/exam/kms_exam_portlet/kmsExamActivityPortlet.do?method=getExamType&rowsize=6&type=near&t=1721991705559&s_ajax=true"
            ,"jsonArray",null,"student"),
    //国招工作台-文本库
    TEXT_LIBRARY("TextLibrary","/kms/multidoc/?j_iframe=true&j_aside=false&toggleView=rowtable"
            ,"/kms/multidoc/kms_multidoc_portlet/kmsMultidocKnowledgePortlet.do?method=getKmsMultidocKnowledge&rowsize=6&type=docPublishTime&categoryId=&dataType=col&t=1721992135538&s_ajax=true"
            ,"jsonArray",null,"textLibrary"),
    //国招工作台-待分配协议
    PENDING_ALLOCATION_AGREEMENT("WaitAlloAgreement",""
            ,"/sys/common/dataxml.jsp?s_bean=sitcProjectMainPortletService&cateid=&rowsize=20&type=&scope=&t=1729476301999&s_ajax=true"
            ,"xml",null,""),
    //业主门户-组织结构图
    ORG_STRUCT_DIAGRAM("OrgStructDiagram","/kms/multidoc/?j_iframe=true&j_aside=false&toggleView=rowtable"
            ,"/resource/proprietor/km_proprietor_owner/kmProprietorOwner.do?method=getOrgData&t=1729590206234&s_ajax=true"
            ,"json",null,""),
    //供应商门户-已领购公告
    PURCHASE_ANNOUNCEMENT("purchaseAnnouncement","/km/professional/km_professional_sign/?j_iframe=true&j_aside=false"
            ,"/sys/common/dataxml.jsp?s_bean=KmSupplierBoarPortlet&fdCategoryId=&rowsize=6&scope=no&showIntroduced=true&t=1722159517841&s_ajax=true"
            ,"xml",new String[]{"id","text","catehref","href","otherinfo","title","catename","creator"},""),
    //供应商门户-采购公告
    PROCUREMENT_NOTICE_SUPPLIER("PurchaseNoticeSupplier","/sys/news/?j_iframe=true&j_aside=false&categoryId=13f89f0aecc9570a269328c4ea8b510d#j_path=%2FdocCategory&cri.q=displayRange%3A35%3BdocStatus%3A30&docCategory=13f89f0aecc9570a269328c4ea8b510d"
            ,"/sys/common/dataxml.jsp?s_bean=sysNewsMainPortletService&cateid=13f89f0aecc9570a269328c4ea8b510d&rowsize=6&type=main&purchase=purchasing&t=1722159517842&s_ajax=true"
            ,"xml",new String[]{"text","catehref","publishTime","width","creator","id","height","importance","created","href","catename","fdAuthor"},"purchasing"),
    //业主门户-我的项目
    MY_PROJECT("SupplierProject","/km/proprietor/km_proprietor_project/index_project.jsp?j_iframe=true&j_aside=false"
            ,"/sys/common/dataxml.jsp?s_bean=KmProprietorBoarPortlet&fdCategoryId=&rowsize=6&scope=no&showIntroduced=true&t=1722160053923&s_ajax=true"
            ,"xml",new String[]{"id","text","href"},"proprietor-project"),
    //业主门户-年度项目统计
    YEAR_PROJECT_STATISTICS("EchartReport",""
            ,"/km/proprietor/km_proprietor_owner/kmProprietorOwner.do?method=listPortletvar1&t=1722160054386&s_ajax=true"
            ,"json",null,""),
    //业主门户-项目类别统计
    PROJECT_type_STATISTICS("projectTypeStatistics",""
            ,"/km/proprietor/km_proprietor_owner/kmProprietorOwner.do?method=listPortletvar2&t=1722160054396&s_ajax=true"
            ,"json",null,""),
    //业主门户-分子公司项目情况
    CHILD_PROJECT("childProject",""
            ,"/km/proprietor/km_proprietor_owner/kmProprietorOwner.do?method=listPortletvar3&t=1722160054405&s_ajax=true"
            ,"json",null,""),
    //项目会议提醒
    PROJECT_MEETING_REMINDER("UserProject",""
            ,""
            ,"jsonArray",null,""),
    //待办
    TODO("Todo","/sys/notify/index.jsp?j_iframe=true&j_aside=false#j_path=%2Fprocess&dataType=todo"
            ,"/api/sys-notify/sysNotifyTodoRestService/getTodo"
            ,"jsonArray",null,"notify-process");





    //组件key
    private final String key;
    //请求url
    private final String reqUrl;
    //列表url
    private final String listUrl;
    //xml格式下列表字段
    private final String[] listFields;
    //返回参数类型
    private final String returnType;
    //菜单导航key
    private final String pageKey;


    AssemblyEnums(String key, String reqUrl, String listUrl, String returnType,String []listFields,String pageKey) {
        this.key = key;
        this.reqUrl = reqUrl;
        this.listUrl = listUrl;
        this.returnType = returnType;
        this.listFields = listFields;
        this.pageKey = pageKey;
    }

    /**
     * 根据组件key获取组件数据
     * @param key       组件key
     * @return          组件数据
     */
    public static AssemblyEnums getAssembly(String key) {
        for (AssemblyEnums assembly : AssemblyEnums.values()) {
            if (assembly.getKey().equals(key)) {
                return assembly;
            }
        }
        return null;
    }
}
