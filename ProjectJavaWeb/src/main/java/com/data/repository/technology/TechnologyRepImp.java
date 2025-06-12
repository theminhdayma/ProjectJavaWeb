package com.data.repository.technology;

import com.data.entity.Technology;
import com.data.entity.enums.Status;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TechnologyRepImp implements TechnologyRep {

    private final SessionFactory sessionFactory;

    @Override
    public List<Technology> findAllTechnologies(int page, int size) {
        Session session = null;
        List<Technology> technologies = null;
        try {
            session = sessionFactory.openSession();
            technologies = session.createQuery("FROM Technology WHERE status = 'ACTIVE'", Technology.class)
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
        return technologies;
    }

    @Override
    public List<Technology> findTechnologiesByName(String name, int page, int size) {
        Session session = null;
        List<Technology> technologies = null;
        try {
            session = sessionFactory.openSession();
            String hql = "FROM Technology WHERE name LIKE :name AND status = 'ACTIVE'";
            technologies = session.createQuery(hql, Technology.class)
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
        return technologies;
    }

    @Override
    public long countAllTechnologies() {
        Session session = null;
        long count = 0;
        try {
            session = sessionFactory.openSession();
            count = (long) session.createQuery("SELECT COUNT(t) FROM Technology t WHERE t.status = 'ACTIVE'").uniqueResult();
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
    public long countTechnologiesByName(String name) {
        Session session = null;
        long count = 0;
        try {
            session = sessionFactory.openSession();
            count = (long) session.createQuery("SELECT COUNT(t) FROM Technology t WHERE t.name LIKE :name AND t.status = 'ACTIVE'")
                                  .setParameter("name", "%" + name + "%")
                                  .uniqueResult();
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
    public Technology findTechnologyById(int id) {
        Session session = null;
        Technology technology = null;
        try {
            session = sessionFactory.openSession();
            technology = session.get(Technology.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return technology;
    }

    @Override
    public boolean deleteTechnology(int id) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            Technology technology = session.get(Technology.class, id);
            if (technology != null) {
                if ((technology.getCandidates() != null && !technology.getCandidates().isEmpty()) ||
                        (technology.getPositions() != null && !technology.getPositions().isEmpty())) {
                    return false;
                }

                technology.setStatus(Status.INACTIVE);
                session.update(technology);
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
    public boolean saveTechnology(Technology technology) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(technology);
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
    public boolean updateTechnology(Technology technology) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            session.update(technology);
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
    public boolean checkNameTechnology(String name) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            long count = (long) session.createQuery("SELECT COUNT(t) FROM Technology t WHERE t.name = :name AND t.status = 'ACTIVE'")
                                       .setParameter("name", name)
                                       .uniqueResult();
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

}
