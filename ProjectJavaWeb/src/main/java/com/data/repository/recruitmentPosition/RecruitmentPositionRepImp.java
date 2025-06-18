package com.data.repository.recruitmentPosition;

import com.data.entity.RecruitmentPosition;
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
    public List<RecruitmentPosition> findAll(int page, int size) {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            return session.createQuery("FROM RecruitmentPosition", RecruitmentPosition.class)
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
}
