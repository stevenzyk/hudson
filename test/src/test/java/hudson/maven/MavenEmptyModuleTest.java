package hudson.maven;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.ExtractResourceSCM;


import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Bayer
 */
public class MavenEmptyModuleTest extends HudsonTestCase {
    /**
     * Verify that a build will work with a module <module></module> and a module <module> </module>
     */
    @Bug(4442)
    public void testEmptyModuleParsesAndBuilds() throws Exception {
        configureDefaultMaven();
        MavenModuleSet m = createMavenProject();
        m.getReporters().add(new TestReporter());
        m.setScm(new ExtractResourceSCM(getClass().getResource("maven-empty-mod.zip")));
        assertBuildStatusSuccess(m.scheduleBuild2(0).get());
    }
    
    private static class TestReporter extends MavenReporter {
        @Override
        public boolean end(MavenBuild build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            assertNotNull(build.getProject().getWorkspace());
            assertNotNull(build.getWorkspace());
            return true;
        }
    }
}