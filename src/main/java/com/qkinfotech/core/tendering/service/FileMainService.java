package com.qkinfotech.core.tendering.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.qkinfotech.core.file.FileTransferController;
import com.qkinfotech.core.file.SysFile;
import com.qkinfotech.core.mvc.SimpleService;
import com.qkinfotech.core.mvc.util.JSONQuerySpecification;
import com.qkinfotech.core.tendering.model.apps.project.AppsProjectPackage;
import com.qkinfotech.core.tendering.model.attachment.AttachmentMain;
import com.qkinfotech.core.tendering.model.attachment.AttachmentPackage;
import com.qkinfotech.util.SpringUtil;
import com.qkinfotech.util.StringUtil;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;
import java.util.List;


@Service
public class FileMainService {

    @Autowired
    protected SimpleService<SysFile> sysFileService;
    @Autowired
    protected SimpleService<AttachmentMain> attachmentMainService;
    @Autowired
    protected SimpleService<AttachmentPackage> attachmentPackageService;
    @Autowired
    protected SimpleService<AppsProjectPackage> appsProjectPackageService;

    /**
     * 查询 att main 附件
     *
     * @param fid 附件的modelid
     * @return
     */
    public List<AttachmentMain> getAttMain(String fModelName, String fid, String fKey) {
        JSONObject query = new JSONObject();
        JSONObject queryJson = new JSONObject();
        JSONArray and = new JSONArray();

        JSONObject eq1 = new JSONObject();
        JSONObject modelName = new JSONObject();
        modelName.put("fModelName", fModelName);
        eq1.put("eq", modelName);
        and.add(eq1);

        JSONObject eq2 = new JSONObject();
        JSONObject modelId = new JSONObject();
        modelId.put("fModelId", fid);
        eq2.put("eq", modelId);
        and.add(eq2);

        JSONObject eq3 = new JSONObject();
        JSONObject modelKey = new JSONObject();
        modelKey.put("fKey", fKey);
        eq3.put("eq", modelKey);
        and.add(eq3);

        queryJson.put("and", and);
        query.put("query", queryJson);
        return attachmentMainService.findAll(JSONQuerySpecification.getSpecification(query));
    }

    /**
     * 数据流转MultipartFile文件 存进 sys file
     *
     * @param inputStream
     * @param fileName
     * @throws Exception
     */
    public String saveInputStream(InputStream inputStream, String fileName) throws Exception {
//        InputStream inputStream = Files.newInputStream(path);
        //数据流转MultipartFile文件
        MultipartFile targetMultipartFile = new MockMultipartFile(fileName, inputStream);
        JSONObject object = SpringUtil.getContext().getBean(FileTransferController.class).uploadFile(targetMultipartFile, fileName);
        return object.getString("fId");//附件 sysfileid
    }

    /**
     * 保存 att main 并且创建 包件关联
     *
     * @param fileId  sys file的id
     * @param fileName
     * @param fModelName
     * @param fModelId
     * @param fKey
     * @param packageIds 需要关联的包件ids
     */
    public void saveAttMain(AttachmentMain attMain,String fileId, String fileName, String fModelName, String fModelId, String fKey, String packageIds) {
        //用fileid 和 attachment_main 创建关系
//        AttachmentMain attMain = new AttachmentMain();
        SysFile sysFile = sysFileService.getById(fileId);
        attMain.setfFile(sysFile);
//        attMain.setfKey("supplierMain");
        attMain.setfKey(fKey);
        attMain.setfFileName(fileName);
//        attMain.setfModelName("com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain");
        attMain.setfModelName(fModelName);
        attMain.setfModelId(fModelId);
        attMain.setfFileSize(String.valueOf(sysFile.getfSize()));
        attMain.setfCreateTime(new Date());
        attachmentMainService.save(attMain);
        //然后再创建关联包件记录
        if(StringUtil.isNotNull(packageIds)){
            String[] codes = packageIds.split(";");
            for (int l = 0; l < codes.length; l++) {
                AppsProjectPackage appsProjectPackage = appsProjectPackageService.getById(codes[l]);
                AttachmentPackage prjPackage = new AttachmentPackage();
                prjPackage.setfPackageId(appsProjectPackage);
                prjPackage.setfAttachmentId(attMain);
                attachmentPackageService.save(prjPackage);
            }
        }
    }

    public List getSupplierFiles(String fProtocolNo,String fProjectName,String fProjectManager,String fCompanyName,String fDesignersTotalType,
                                 Integer fDesignersTotal,String fName,String fDesignQualificationTypeOrLevel,String fProfessionalQualification,
                                 String fCountry,String fOwnerName,String fNorm,String fFunctionality,String fProjectPlace,String fExperienceYearsType,Integer fExperienceYears){
        String sql="SELECT DISTINCT (promain.fName,promain.fProtocolNo,att.fFileName,att.fFile.fId) FROM com.qkinfotech.core.tendering.model.attachment.AttachmentMain att " +
                "LEFT JOIN com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain supmain ON att.fModelId = supmain.fId " +
                "LEFT JOIN com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierInfo info " +
                "ON supmain.fId=info.fSupplierId.fId LEFT JOIN com.qkinfotech.core.tendering.model.apps.project.AppsProjectMain promain ON supmain.fProjectId.fId=promain.fId " +
                "LEFT JOIN com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerMain designer ON supmain.fId=designer.fSupplierId.fId " +
                "LEFT JOIN com.qkinfotech.core.tendering.model.apps.designer.AppsDesignerAchievement achievement ON supmain.fId=achievement.fSupplierId.fId " +
                "WHERE att.fModelName=:fModelName " +
                "AND supmain.fCurrentStatus=:fCurrentStatus ";
        if(StringUtil.isNotNull(fProtocolNo)){
            sql+=" and promain.fProtocolNo like :fProtocolNo ";
        }
        if(StringUtil.isNotNull(fProjectName)){
            sql+=" and promain.fName like :fProjectName ";
        }
        if(StringUtil.isNotNull(fProjectManager)){
            sql+=" and promain.fDeptManager.fId = :fProjectManager ";
        }
        if(StringUtil.isNotNull(fCompanyName)){
            sql+=" and info.fCompanyName like :fCompanyName ";
        }
        if(StringUtil.isNotNull(fDesignersTotalType)&&fDesignersTotal!=null){
            if("=".equals(fDesignersTotalType)){
                sql+=" and info.fDesignersTotal = :fDesignersTotal ";
            }else if(">".equals(fDesignersTotalType)){
                sql+=" and info.fDesignersTotal > :fDesignersTotal ";
            }else{
                sql+=" and info.fDesignersTotal < :fDesignersTotal ";
            }

        }

        if(StringUtil.isNotNull(fName)){
            sql+=" and designer.fName like :fName ";
        }

        if(StringUtil.isNotNull(fDesignQualificationTypeOrLevel)){
            sql+=" and info.fDesignQualificationTypeOrLevel like :fDesignQualificationTypeOrLevel ";
        }

        if(StringUtil.isNotNull(fProfessionalQualification)){
            sql+=" and designer.fProfessionalQualification like :fProfessionalQualification ";
        }
        if(StringUtil.isNotNull(fCountry)){
            sql+=" and info.fCountry like :fCountry ";
        }
        if(StringUtil.isNotNull(fOwnerName)){
            sql+=" and achievement.fOwnerName like :fOwnerName ";
        }
        if(StringUtil.isNotNull(fNorm)){
            sql+=" and achievement.fNorm like :fNorm ";
        }
        if(StringUtil.isNotNull(fFunctionality)){
            sql+=" and achievement.fFunctionality like :fFunctionality ";
        }
        if(StringUtil.isNotNull(fProjectPlace)){
            sql+=" and achievement.fProjectPlace like :fProjectPlace ";
        }
        if(StringUtil.isNotNull(fExperienceYearsType)&&fExperienceYears!=null){
            if("=".equals(fExperienceYearsType)){
                sql+=" and designer.fExperienceYears = :fExperienceYears ";
            }else if(">".equals(fExperienceYearsType)){
                sql+=" and designer.fExperienceYears > :fExperienceYears ";
            }else{
                sql+=" and designer.fExperienceYears < :fExperienceYears ";
            }

        }

        Query query = sysFileService.getRepository().getEntityManager().createQuery(sql);
        query.setParameter("fModelName","com.qkinfotech.core.tendering.model.apps.supplier.AppsSupplierMain");
        query.setParameter("fCurrentStatus","2");
        if(StringUtil.isNotNull(fProtocolNo)){
            query.setParameter("fProtocolNo","%"+fProtocolNo+"%");
        }
        if(StringUtil.isNotNull(fProjectName)){
            query.setParameter("fProjectName","%"+fProjectName+"%");
        }
        if(StringUtil.isNotNull(fProjectManager)){
            query.setParameter("fProjectManager",fProjectManager);
        }
        if(StringUtil.isNotNull(fCompanyName)){
            query.setParameter("fCompanyName","%"+fCompanyName+"%");
        }
        if(StringUtil.isNotNull(fDesignersTotalType)&&fDesignersTotal!=null){
            query.setParameter("fDesignersTotal",fDesignersTotal);
        }
        if(StringUtil.isNotNull(fName)){
            query.setParameter("fName","%"+fName+"%");
        }

        if(StringUtil.isNotNull(fDesignQualificationTypeOrLevel)){
            query.setParameter("fDesignQualificationTypeOrLevel","%"+fDesignQualificationTypeOrLevel+"%");
        }
        if(StringUtil.isNotNull(fProfessionalQualification)){
            query.setParameter("fProfessionalQualification","%"+fProfessionalQualification+"%");
        }
        if(StringUtil.isNotNull(fCountry)){
            query.setParameter("fCountry","%"+fCountry+"%");
        }
        if(StringUtil.isNotNull(fOwnerName)){
            query.setParameter("fOwnerName","%"+fOwnerName+"%");
        }
        if(StringUtil.isNotNull(fNorm)){
            query.setParameter("fNorm","%"+fNorm+"%");
        }
        if(StringUtil.isNotNull(fFunctionality)){
            query.setParameter("fFunctionality","%"+fFunctionality+"%");
        }
        if(StringUtil.isNotNull(fProjectPlace)){
            query.setParameter("fProjectPlace","%"+fProjectPlace+"%");
        }

        if(StringUtil.isNotNull(fExperienceYearsType)&&fExperienceYears!=null){
            query.setParameter("fExperienceYears",fExperienceYears);

        }

        return query.getResultList();
    }

}
