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

    @Override
    public List<Candidate> getFilteredCandidates(String search, String gender, Integer age,
                                                 Integer experience, Integer technologyId,
                                                 int page, int size) {
        Session session = null;
        List<Candidate> candidates = new ArrayList<>();
        try {
            session = sessionFactory.openSession();

            // QUERY 1: Lấy IDs với pagination chính xác
            StringBuilder hqlIds = new StringBuilder("SELECT DISTINCT c.id FROM Candidate c");

            // Nếu có filter theo technology thì cần JOIN
            if (technologyId != null) {
                hqlIds.append(" JOIN c.technologies t");
            }

            Map<String, Object> parameters = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            buildWhereConditions(search, gender, age, experience, conditions, parameters);

            // Thêm điều kiện lọc theo công nghệ nếu có
            if (technologyId != null) {
                conditions.add("t.id = :technologyId");
                parameters.put("technologyId", technologyId);
            }

            if (!conditions.isEmpty()) {
                hqlIds.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            hqlIds.append(" ORDER BY c.id");

            Query<Integer> idsQuery = session.createQuery(hqlIds.toString(), Integer.class);
            setQueryParameters(idsQuery, parameters);

            // Áp dụng pagination
            List<Integer> candidateIds = idsQuery
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .list();

            // QUERY 2: Lấy full entities với JOIN FETCH dựa trên IDs
            if (!candidateIds.isEmpty()) {
                String fetchHql = "SELECT DISTINCT c FROM Candidate c " +
                        "LEFT JOIN FETCH c.technologies t " +
                        "LEFT JOIN FETCH c.account " +
                        "WHERE c.id IN (:ids) " +
                        "ORDER BY c.id";

                Query<Candidate> fetchQuery = session.createQuery(fetchHql, Candidate.class);
                fetchQuery.setParameter("ids", candidateIds);
                candidates = fetchQuery.list();
            }

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
                                        Integer experience, Integer technologyId) {
        Session session = null;
        long count = 0;
        try {
            session = sessionFactory.openSession();

            StringBuilder hql = new StringBuilder("SELECT COUNT(DISTINCT c.id) FROM Candidate c");

            // Nếu có filter theo technology thì cần JOIN
            if (technologyId != null) {
                hql.append(" JOIN c.technologies t");
            }

            Map<String, Object> parameters = new HashMap<>();
            List<String> conditions = new ArrayList<>();

            buildWhereConditions(search, gender, age, experience, conditions, parameters);

            // Thêm điều kiện lọc theo công nghệ nếu có
            if (technologyId != null) {
                conditions.add("t.id = :technologyId");
                parameters.put("technologyId", technologyId);
            }

            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            setQueryParameters(query, parameters);

            count = query.uniqueResult();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return count;
    }


    @Override
    public boolean updateCandidate(Candidate candidate) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.update(candidate);
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (session != null && session.getTransaction().isActive()) {
                session.getTransaction().rollback();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // Phương thức helper để xây dựng điều kiện WHERE - CHỈ TÌM KIẾM THEO TÊN
    private void buildWhereConditions(String search, String gender, Integer age,
                                      Integer experience, List<String> conditions,
                                      Map<String, Object> parameters) {

        // Tìm kiếm theo tên (không phân biệt hoa thường)
        if (search != null && !search.trim().isEmpty()) {
            conditions.add("LOWER(c.name) LIKE LOWER(:search)");
            parameters.put("search", "%" + search.trim() + "%");
        }

        // Lọc theo giới tính
        if (gender != null && !gender.isEmpty()) {
            conditions.add("c.gender = :gender");
            parameters.put("gender", Gender.valueOf(gender));
        }

        // Lọc theo tuổi tối thiểu
        if (age != null) {
            conditions.add("YEAR(CURRENT_DATE) - YEAR(c.dob) >= :age");
            parameters.put("age", age);
        }

        // Lọc theo kinh nghiệm tối thiểu
        if (experience != null) {
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
