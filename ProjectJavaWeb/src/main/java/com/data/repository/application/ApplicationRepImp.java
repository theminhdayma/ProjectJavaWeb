package com.data.repository.application;

import com.data.entity.Application;
import com.data.entity.enums.Progress;
import com.data.entity.enums.ResultInterview;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApplicationRepImp implements ApplicationRep {

    private final SessionFactory sessionFactory;

    @Override
    public List<Application> findAll(String keyword, Progress progress, ResultInterview resultInterview, int page, int size) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String hql = "FROM Application a WHERE (:keyword IS NULL OR a.candidate.name LIKE :keyword) " +
                         "AND (:progress IS NULL OR a.progress = :progress) " +
                         "AND (:resultInterview IS NULL OR a.resultInterview = :resultInterview)";
            Query<Application> query = session.createQuery(hql, Application.class);
            query.setParameter("keyword", keyword != null ? "%" + keyword + "%" : null);
            query.setParameter("progress", progress);
            query.setParameter("resultInterview", resultInterview);
            query.setFirstResult(page * size);
            query.setMaxResults(size);
            return query.list();
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
    public List<Application> findAllByCandidateId(int candidateId, int page, int size) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return session.createQuery("FROM Application WHERE candidate.id = :candidateId", Application.class)
                    .setParameter("candidateId", candidateId)
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
    public Application findById(int id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return session.get(Application.class, id);
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
    public Application save(Application application) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(application);
            session.getTransaction().commit();
            return application;
        } catch (Exception e) {
            if (session.getTransaction() != null) {
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
    public long countAll(String keyword, Progress progress, ResultInterview resultInterview) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String hql = "SELECT COUNT(a) FROM Application a WHERE (:keyword IS NULL OR a.candidate.name LIKE :keyword) " +
                         "AND (:progress IS NULL OR a.progress = :progress) " +
                         "AND (:resultInterview IS NULL OR a.resultInterview = :resultInterview)";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("keyword", keyword != null ? "%" + keyword + "%" : null);
            query.setParameter("progress", progress);
            query.setParameter("resultInterview", resultInterview);
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
    public long countByCandidateId(int candidateId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return (Long) session.createQuery("SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId")
                    .setParameter("candidateId", candidateId)
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
    public boolean update(Application application) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.update(application);
            session.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (session.getTransaction() != null) {
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
