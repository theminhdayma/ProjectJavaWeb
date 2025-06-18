package com.data.repository.application;

import com.data.entity.Application;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApplicationRepImp implements ApplicationRep {

    private final SessionFactory sessionFactory;

    @Override
    public List<Application> findAll(int page, int size) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return session.createQuery("FROM Application", Application.class)
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
    public boolean save(Application application) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.saveOrUpdate(application);
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
