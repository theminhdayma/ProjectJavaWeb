package com.data.repository.candidate;

import com.data.entity.Candidate;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CandidateRepImp implements CandidateRep {

    private final SessionFactory sessionFactory;

    @Override
    public boolean register(Candidate candidate) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(candidate);
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
    public List<Candidate> getCandidates(int page, int size) {
        Session session = null;
        List<Candidate> candidates = null;
        try {
            session = sessionFactory.openSession();
            candidates = session.createQuery("FROM Candidate", Candidate.class)
                    .setFirstResult(page * size)
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
    public List<Candidate> getCandidatesByName(String name, int page, int size) {
        Session session = null;
        List<Candidate> candidates = null;
        try {
            session = sessionFactory.openSession();
            String hql = "FROM Candidate WHERE name LIKE :name";
            candidates = session.createQuery(hql, Candidate.class)
                    .setParameter("name", "%" + name + "%")
                    .setFirstResult(page * size)
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
    public long countAllCandidates() {
        Session session = null;
        long count = 0;
        try {
            session = sessionFactory.openSession();
            count = (long) session.createQuery("SELECT COUNT(c) FROM Candidate c").uniqueResult();
            return count;
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
    public long countCandidatesByName(String name) {
        Session session = null;
        long count = 0;
        try {
            session = sessionFactory.openSession();
            String hql = "SELECT COUNT(c) FROM Candidate c WHERE c.name LIKE :name";
            count = (long) session.createQuery(hql)
                    .setParameter("name", "%" + name + "%")
                    .uniqueResult();
            return count;
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

}
