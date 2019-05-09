package model;

import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.List;

@Data
public class ProfileStatistics {

    @Expose
    private Long solvedRepositories;
    @Expose
    private Long unsolvedRepositories;
    @Expose
    private Long failedRepositories;
    @Expose
    private Long totalRepositories;
    @Expose
    private List<Repository> dueDateClosingRepositories;
    @Expose
    private Organization latestOrganization;


    public ProfileStatistics(Long solvedRepositories, Long unsolvedRepositories,Long failedRepositories, Long totalRepositories , List<Repository> dueDateClosingRepositories, Organization latestOrganization) {
        this.solvedRepositories = solvedRepositories;
        this.unsolvedRepositories = unsolvedRepositories;
        this.failedRepositories = failedRepositories;
        this.totalRepositories = totalRepositories;
        this.dueDateClosingRepositories = dueDateClosingRepositories;
        this.latestOrganization = latestOrganization;
    }
}
