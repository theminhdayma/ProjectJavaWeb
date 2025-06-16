package com.data.repository.account;

import com.data.entity.Account;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AccountRepImp implements AccountRep {

    private final SessionFactory sessionFactory;

    @Override
    public boolean login(String email, String password) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query query = session.createQuery("FROM Account WHERE email = :email AND password = :password AND status = 'ACTIVE'");
            query.setParameter("email", email);
            query.setParameter("password", password);
            return query.uniqueResult() != null;
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
    public boolean register(Account account) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(account);
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
    public boolean checkEmail(String email) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query query = session.createQuery("SELECT COUNT(*) FROM Account WHERE email = :email");
            query.setParameter("email", email);
            Long count = (Long) query.uniqueResult();
            return count > 0;
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
    public Account findAccountByEmail(String email) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<Account> query = session.createQuery("FROM Account WHERE email = :email", Account.class);
            query.setParameter("email", email);
            return query.uniqueResult();
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
    public boolean resetPassword(String email, String newPassword) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            Query query = session.createQuery("UPDATE Account SET password = :newPassword WHERE email = :email");
            query.setParameter("newPassword", newPassword);
            query.setParameter("email", email);
            int result = query.executeUpdate();
            session.getTransaction().commit();
            return result > 0;
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
    public boolean lockCandidate(int id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            Query query = session.createQuery("UPDATE Account SET status = 'INACTIVE' WHERE id = :id");
            query.setParameter("id", id);
            int result = query.executeUpdate();
            session.getTransaction().commit();
            return result > 0;
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
    public boolean unlockCandidate(int id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            Query query = session.createQuery("UPDATE Account SET status = 'ACTIVE' WHERE id = :id");
            query.setParameter("id", id);
            int result = query.executeUpdate();
            session.getTransaction().commit();
            return result > 0;
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
    public Account findAccountByCandidateId(int candidateId) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<Account> query = session.createQuery("FROM Account WHERE candidate.id = :candidateId", Account.class);
            query.setParameter("candidateId", candidateId);
            return query.uniqueResult();
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
