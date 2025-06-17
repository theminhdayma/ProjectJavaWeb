package com.data.repository.candidate;

import com.data.entity.Candidate;
import com.data.entity.enums.Gender;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CandidateRepImp implements CandidateRep {

    private final SessionFactory sessionFactory;

    @Override
    public Candidate register(Candidate candidate) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(candidate);
            session.getTransaction().commit();
            return candidate;
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public boolean checkPhoneNumber(String phoneNumber) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String hql = "FROM Candidate WHERE phone = :phoneNumber";
            return session.createQuery(hql, Candidate.class)
                    .setParameter("phoneNumber", phoneNumber)
                    .uniqueResult() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public Candidate getCandidateById(int id) {
        Session session = null;
        Candidate candidate = null;
        try {
            session = sessionFactory.openSession();
            candidate = session.get(Candidate.class, id);
            return candidate;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // PHƯƠNG THỨC LỌC TỔNG HỢP - CHỈ TÌM KIẾM THEO TÊN
    @Override
    public List<Candidate> getFilteredCandidates(String search, String gender, Integer age,
                                                 Integer experience, int page, int size) {
        Session session = null;
        List<Candidate> candidates = null;
        try {
            session = sessionFactory.openSession();

            StringBuilder hql = new StringBuilder(
                    "SELECT DISTINCT c FROM Candidate c " +
                            "LEFT JOIN FETCH c.technologies " +  // Eager fetch technologies
                            "LEFT JOIN FETCH c.account"          // Eager fetch account
            );

            Map<String, Object> parameters = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            buildWhereConditions(search, gender, age, experience, conditions, parameters);

            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            hql.append(" ORDER BY c.id");

            Query<Candidate> query = session.createQuery(hql.toString(), Candidate.class);
            setQueryParameters(query, parameters);

            candidates = query.setFirstResult(page * size)
                    .setMaxResults(size)
                    .list();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return candidates;
    }


    @Override
    public long countFilteredCandidates(String search, String gender, Integer age,
                                        Integer experience) {
        Session session = null;
        long count = 0;
        try {
            session = sessionFactory.openSession();

            StringBuilder hql = new StringBuilder("SELECT COUNT(DISTINCT c) FROM Candidate c");
            Map<String, Object> parameters = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            // Xây dựng điều kiện WHERE
            buildWhereConditions(search, gender, age, experience, conditions, parameters);

            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            Query query = session.createQuery(hql.toString());
            setQueryParameters(query, parameters);

            count = (long) query.uniqueResult();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return count;
    }

    // Phương thức helper để xây dựng điều kiện WHERE - CHỈ TÌM KIẾM THEO TÊN
    private void buildWhereConditions(String search, String gender, Integer age, Integer experience,
                                      List<String> conditions, Map<String, Object> parameters) {

        // CHỈ tìm kiếm theo tên (bỏ email)
        if (search != null && !search.trim().isEmpty()) {
            conditions.add("LOWER(c.name) LIKE LOWER(:search)");
            parameters.put("search", "%" + search.trim() + "%");
        }

        // Lọc theo giới tính
        if (gender != null && !gender.trim().isEmpty()) {
            try {
                Gender genderEnum = Gender.valueOf(gender.toUpperCase());
                conditions.add("c.gender = :gender");
                parameters.put("gender", genderEnum);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid gender value: " + gender);
            }
        }

        // Lọc theo tuổi (tuổi tối thiểu)
        if (age != null && age > 0) {
            LocalDate targetDate = LocalDate.now().minusYears(age);
            conditions.add("c.dob <= :targetDate");
            parameters.put("targetDate", targetDate);
        }

        // Lọc theo kinh nghiệm (kinh nghiệm tối thiểu)
        if (experience != null && experience >= 0) {
            conditions.add("c.experience >= :experience");
            parameters.put("experience", experience);
        }
    }

    // Phương thức helper để set parameters cho query
    private void setQueryParameters(Query query, Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }
}
