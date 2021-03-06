package com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.syntacticsugar;

import com.google.common.base.Supplier;
import com.sonyericsson.jenkins.plugins.bfa.model.FailureCauseBuildAction;
import hudson.model.*;
import hudson.plugins.claim.ClaimBuildAction;
import hudson.scm.ChangeLogSet;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.bfa.AnalysedTest.failureCauseBuildAction;
import static com.smartcodeltd.jenkinsci.plugins.buildmonitor.viewmodel.plugins.claim.ClaimedTest.claimBuildAction;
import static org.mockito.Mockito.*;

/**
 * @author Jan Molak
 */
public class BuildStateRecipe implements Supplier<AbstractBuild<?, ?>> {

    private AbstractBuild<?, ?> build;

    public BuildStateRecipe() {
        build = mock(AbstractBuild.class);

        AbstractProject parent = mock(AbstractProject.class);
        when(build.getParent()).thenReturn(parent);
    }

    public BuildStateRecipe hasNumber(int number) {
        when(build.getNumber()).thenReturn(number);

        // see hudson.model.Run::getDisplayName
        return hasName("#" + number);
    }

    public BuildStateRecipe hasName(String name) {
        when(build.getDisplayName()).thenReturn(name);

        return this;
    }

    public BuildStateRecipe whichNumberIs(int number) {
        return hasNumber(number);
    }

    public BuildStateRecipe finishedWith(Result result) {
        when(build.getResult()).thenReturn(result);

        return this;
    }

    public BuildStateRecipe withChangesFrom(String... authors) {
        ChangeLogSet changeSet = changeSetBasedOn(entriesBy(authors));
        when(build.getChangeSet()).thenReturn(changeSet);

        // any methods that use getChangeSet as their source of data should be called normally
        // (build is a partial mock in this case)
        when(build.getCulprits()).thenCallRealMethod();

        return this;
    }

    public BuildStateRecipe succeededThanksTo(String... authors) {
        finishedWith(Result.SUCCESS);
        withChangesFrom(authors);

        return this;
    }

    public BuildStateRecipe wasBrokenBy(String... culprits) {
        finishedWith(Result.FAILURE);
        withChangesFrom(culprits);

        return this;
    }

    public BuildStateRecipe hasntStartedYet() {
        when(build.hasntStartedYet()).thenReturn(true);

        return this;
    }

    public BuildStateRecipe isStillBuilding() {
        when(build.isBuilding()).thenReturn(true);

        return this;
    }

    public BuildStateRecipe isStillUpdatingTheLog() {
        when(build.isLogUpdated()).thenReturn(true);

        return this;
    }

    public BuildStateRecipe startedAt(String time) throws Exception {
        long startedAt = new SimpleDateFormat("H:m:s").parse(time).getTime();

        Calendar timestamp = mock(Calendar.class);
        when(build.getTimestamp()).thenReturn(timestamp);

        when(timestamp.getTimeInMillis()).thenReturn(startedAt);

        return this;
    }

    public BuildStateRecipe took(int minutes) throws Exception{
        long duration = (long) minutes * 60 * 1000;

        when(build.getDuration()).thenReturn(duration);

        return this;
    }

    public BuildStateRecipe usuallyTakes(int minutes) throws Exception{
        long duration = (long) minutes * 60 * 1000;

        when(build.getEstimatedDuration()).thenReturn(duration);

        return this;
    }

    public BuildStateRecipe wasClaimedBy(String aPotentialHero, String reason) {
        final ClaimBuildAction action = claimBuildAction(aPotentialHero, reason);
        when(build.getAction(ClaimBuildAction.class)).thenReturn(action);

        return this;
    }

    public BuildStateRecipe knownFailures(String... failures) {
        final FailureCauseBuildAction action = failureCauseBuildAction(failures);
        when(build.getAction(FailureCauseBuildAction.class)).thenReturn(action);

        return this;
    }

    public BuildStateRecipe and() {
        return this;
    }

    @Override
    public AbstractBuild get() {
        return build;
    }

    private List<ChangeLogSet.Entry> entriesBy(String... authors) {
        List<ChangeLogSet.Entry> entries = new ArrayList<ChangeLogSet.Entry>();

        for (String name : authors) {
            User author = mock(User.class);
            ChangeLogSet.Entry entry = mock(ChangeLogSet.Entry.class);

            when(author.getFullName()).thenReturn(name);
            when(entry.getAuthor()).thenReturn(author);

            entries.add(entry);
        }
        return entries;
    }

    private ChangeLogSet changeSetBasedOn(final List<ChangeLogSet.Entry> entries) {
        return new ChangeLogSet<ChangeLogSet.Entry>(null) {
            @Override
            public boolean isEmptySet() {
                return false;
            }

            public Iterator<Entry> iterator() {
                return entries.iterator();
            }
        };
    }
}