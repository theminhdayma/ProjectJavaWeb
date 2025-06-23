package com.data.repository.recruitmentPosition;

import com.data.entity.Candidate;
import com.data.entity.RecruitmentPosition;
import com.data.entity.enums.Status;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class RecruitmentPositionRepImp implements RecruitmentPositionRep {

    private final SessionFactory sessionFactory;

    @Override
    public List<RecruitmentPosition> findAll(String keyword, int page, int size) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String hql = "SELECT DISTINCT rp FROM RecruitmentPosition rp " +
                    "LEFT JOIN FETCH rp.technologies " +
                    "WHERE rp.description LIKE :keyword OR rp.name LIKE :keyword";
            return session.createQuery(hql, RecruitmentPosition.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .list();
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
    public RecruitmentPosition findById(int id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String hql = "SELECT DISTINCT rp FROM RecruitmentPosition rp " +
                    "LEFT JOIN FETCH rp.technologies " +
                    "WHERE rp.id = :id";
            return session.createQuery(hql, RecruitmentPosition.class)
                    .setParameter("id", id)
                    .uniqueResult();
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
    public boolean save(RecruitmentPosition recruitmentPosition) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(recruitmentPosition);
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

    @Override
    public long countAll(String keyword) { // Thêm tham số keyword
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String hql = "SELECT COUNT(DISTINCT rp) FROM RecruitmentPosition rp " +
                    "WHERE (rp.description LIKE :keyword OR rp.name LIKE :keyword) " +
                    "AND rp.status = 'ACTIVE'";
            return (Long) session.createQuery(hql)
                    .setParameter("keyword", "%" + keyword + "%")
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


    @Override
    public boolean update(RecruitmentPosition recruitmentPosition) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.update(recruitmentPosition);
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

    @Override
    public boolean delete(int id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();

            RecruitmentPosition recruitmentPosition = session.get(RecruitmentPosition.class, id);
            if (recruitmentPosition != null) {
                String checkApplicationQuery = "SELECT COUNT(*) FROM Application a JOIN a.recruitmentPosition rp WHERE rp.id = :id";
                Long count = (Long) session.createQuery(checkApplicationQuery)
                        .setParameter("id", id)
                        .uniqueResult();

                String checkTechnologyQuery = "SELECT COUNT(*) FROM Technology t JOIN t.positions rp WHERE rp.id = :id";
                Long techCount = (Long) session.createQuery(checkTechnologyQuery)
                        .setParameter("id", id)
                        .uniqueResult();
                boolean hasRelations = (count > 0 ) || (techCount > 0);
                if (hasRelations) {
                    recruitmentPosition.setStatus(Status.INACTIVE);
                    session.update(recruitmentPosition);
                } else {
                    session.delete(recruitmentPosition);
                }
                session.getTransaction().commit();
                return true;
            }
            return false;
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

    @Override
    public List<RecruitmentPosition> findAllByTechnology(String keyword, Integer technologyId, int page, int size) {
        Session session = null;
        List<RecruitmentPosition> recruitmentPositions = new ArrayList<>();
        try {
            session = sessionFactory.openSession();

            StringBuilder hql = new StringBuilder("SELECT DISTINCT rp FROM RecruitmentPosition rp " +
                    "LEFT JOIN FETCH rp.technologies t " +
                    "WHERE (rp.description LIKE :keyword OR rp.name LIKE :keyword) " +
                    "AND rp.status = 'ACTIVE'");

            // Thêm điều kiện lọc theo technology nếu có
            if (technologyId != null) {
                hql.append(" AND t.id = :technologyId");
            }

            Query<RecruitmentPosition> query = session.createQuery(hql.toString(), RecruitmentPosition.class);
            query.setParameter("keyword", "%" + keyword + "%");

            if (technologyId != null) {
                query.setParameter("technologyId", technologyId);
            }

            query.setFirstResult(page * size);
            query.setMaxResults(size);

            recruitmentPositions = query.list();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return recruitmentPositions;
    }

    @Override
    public long countAllByTechnology(String keyword, Integer technologyId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            StringBuilder hql = new StringBuilder("SELECT COUNT(DISTINCT rp) FROM RecruitmentPosition rp " +
                    "LEFT JOIN rp.technologies t " +
                    "WHERE (rp.description LIKE :keyword OR rp.name LIKE :keyword) " +
                    "AND rp.status = 'ACTIVE'");

            if (technologyId != null) {
                hql.append(" AND t.id = :technologyId");
            }

            Query<Long> query = session.createQuery(hql.toString(), Long.class);
            query.setParameter("keyword", "%" + keyword + "%");

            if (technologyId != null) {
                query.setParameter("technologyId", technologyId);
            }

            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<RecruitmentPosition> findRandomPositionsExcluding(int excludeId, int limit) {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            // Đếm tổng số positions (trừ position hiện tại)
            String countHql = "SELECT COUNT(rp) FROM RecruitmentPosition rp " +
                    "WHERE rp.id != :excludeId AND rp.status = 'ACTIVE'";
            Query<Long> countQuery = session.createQuery(countHql, Long.class);
            countQuery.setParameter("excludeId", excludeId);
            Long totalCount = countQuery.uniqueResult();

            if (totalCount == null || totalCount == 0) {
                return new ArrayList<>();
            }

            // Tạo random offset
            Random random = new Random();
            int maxOffset = Math.max(0, totalCount.intValue() - limit);
            int randomOffset = random.nextInt(maxOffset + 1);

            // Lấy random positions
            String hql = "SELECT DISTINCT rp FROM RecruitmentPosition rp " +
                    "LEFT JOIN FETCH rp.technologies " +
                    "WHERE rp.id != :excludeId AND rp.status = 'ACTIVE' " +
                    "ORDER BY rp.id";

            Query<RecruitmentPosition> query = session.createQuery(hql, RecruitmentPosition.class);
            query.setParameter("excludeId", excludeId);
            query.setFirstResult(randomOffset);
            query.setMaxResults(limit);

            return query.list();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


}
