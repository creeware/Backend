package controller;

import model.*;
import org.hibernate.Session;
import org.hibernate.query.Query;
import spark.Request;
import spark.Response;
import util.HibernateUtil;

import java.util.*;

public class StatisticsController {
    public static Object getUserStatistics(Request request, Response response) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, 10);
        UUID uuid = UUID.fromString(request.params(":uuid"));

        Long solvedRepositories = (Long) session.createQuery("Select count (repository.id) from Repository repository where user_uuid=:user_uuid AND repository_status='solved' AND repository_admin_uuid!=:user_uuid")
                .setParameter("user_uuid", uuid)
                .uniqueResult();
        Long unsolvedRepositories = (Long) session.createQuery("Select count (repository.id) from Repository repository where user_uuid=:user_uuid AND repository_status='unsolved' AND repository_admin_uuid!=:user_uuid")
                .setParameter("user_uuid", uuid)
                .uniqueResult();
        Long failedRepositories = (Long) session.createQuery("Select count (repository.id) from Repository repository where user_uuid=:user_uuid AND repository_status='failed' AND repository_admin_uuid!=:user_uuid")
                .setParameter("user_uuid", uuid)
                .uniqueResult();
        Long totalRepositories = (Long) session.createQuery("Select count (repository.id) from Repository repository where user_uuid=:user_uuid AND repository_admin_uuid!=:user_uuid")
                .setParameter("user_uuid", uuid)
                .uniqueResult();
        List<Repository> dueDateClosingRepositories = session.createQuery("from Repository repository where user_uuid=:user_uuid AND due_date=:due_date", Repository.class)
                .setParameter("user_uuid", uuid)
                .setParameter("due_date", cal.getTime())
                .getResultList();
        List<Organization> latestOrganizationsList = session.createQuery("from Organization where user_uuid=:user_uuid ORDER BY created_at DESC", Organization.class)
                .setParameter("user_uuid", uuid)
                .getResultList();

        Organization latestOrganization = null;

        if(!latestOrganizationsList.isEmpty()){
            latestOrganization = latestOrganizationsList.get(0);
        }

        session.close();
        response.status(200);
        return new ProfileStatistics(solvedRepositories, unsolvedRepositories, failedRepositories, totalRepositories, dueDateClosingRepositories, latestOrganization);

    }
}
