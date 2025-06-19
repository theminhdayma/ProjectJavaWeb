package com.data.repository.recruitmentPosition;

import com.data.entity.RecruitmentPosition;
import com.data.entity.enums.Status;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecruitmentPositionRepImp implements RecruitmentPositionRep {

    private final SessionFactory sessionFactory;

    @Override
    public List<RecruitmentPosition> findAll(String keyword, int page, int size) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String hql = "FROM RecruitmentPosition WHERE description LIKE :keyword";
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
            return session.get(RecruitmentPosition.class, id);
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
    public long countAll() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return (Long) session.createQuery("SELECT COUNT(*) FROM RecruitmentPosition").uniqueResult();
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

}
