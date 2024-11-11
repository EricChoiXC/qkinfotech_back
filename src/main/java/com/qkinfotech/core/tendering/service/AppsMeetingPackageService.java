package com.qkinfotech.core.tendering.service;

import com.qkinfotech.core.tendering.model.apps.meeting.MeetingMain;
import com.qkinfotech.core.tendering.model.apps.meeting.MeetingPackage;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AppsMeetingPackageService {
    public static Specification<MeetingPackage> hasFModelId(String fModelId) {
        return (root, query, criteriaBuilder) -> {
            Subquery<MeetingMain> subquery = query.subquery(MeetingMain.class);
            Root<MeetingMain> subRoot = subquery.from(MeetingMain.class);
            subquery.select(subRoot.get("fId"));
            subquery.where(criteriaBuilder.equal(subRoot.get("fModelId"), fModelId));
            return root.get("fMeetingId").in(subquery);
        };
    }
    public static Specification<MeetingPackage> hasFModelIds(List<String> fModelIds) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (String fModelId : fModelIds) {
                Subquery<MeetingMain> subquery = query.subquery(MeetingMain.class);
                Root<MeetingMain> subRoot = subquery.from(MeetingMain.class);
                subquery.select(subRoot.get("fId"));
                subquery.where(criteriaBuilder.equal(subRoot.get("fModelId"), fModelId));

                predicates.add(root.get("fMeetingId").in(subquery));
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
