import jenkins.model.*
import hudson.security.*
import hudson.security.csrf.*
import java.util.logging.Logger
import jenkins.util.SystemProperties
import jenkins.security.s2m.*

// more about jenkins "init" Groovy Hook Scripts:
//   https://wiki.jenkins.io/display/JENKINS/Groovy+Hook+Script

new JenkinsBootstrap().init()

class JenkinsBootstrap {

    private static final Logger LOGGER = Logger.getLogger(JenkinsBootstrap.name)

    private jenkins = Jenkins.get()

    void init() {
        
        disableBuiltInStartupWizard()

        restrictAccessToAdminUser()
        
        disableCLIRemoting()
        disableDeprecatedAgentProtocols()
        mitigateCSRF()
        setMasterToSlaveSecurity()

        setWelcomeMessage()

        jenkins.save()

    }

    void disableBuiltInStartupWizard() {

        // more about jenkins.install.runSetupWizard: 
        //   https://wiki.jenkins.io/display/JENKINS/Features+controlled+by+system+properties
        
        LOGGER.info("disabling the built-in setup wizard by setting system property jenkins.install.runSetupWizard=false - this can leave jenkins vulnerable if init scripts don't setup basic security, for example, if the script fails!")

        System.setProperty('jenkins.install.runSetupWizard', 'false')

    }

    void restrictAccessToAdminUser() {
        
        // FYI idempotence wise the core code seemingly doesn't fail if called on each startup

        setHudsonPrivateSecurityRealm()
        addAdminUser()
        setFullControlOnceLoggedInAuthorizationStrategy() 
    
    }

    void setHudsonPrivateSecurityRealm() {
       
        if (jenkins.getSecurityRealm() instanceof HudsonPrivateSecurityRealm) {
            return
        }

        LOGGER.info('setting HudsonPrivateSecurityRealm')
        
        // hide account sign up link on login page
        def allowsOnlineSignup = false
        def realm = new HudsonPrivateSecurityRealm(allowsOnlineSignup)

        jenkins.setSecurityRealm(realm)
        
    }

    void setFullControlOnceLoggedInAuthorizationStrategy() {
        
        if (jenkins.getAuthorizationStrategy() instanceof FullControlOnceLoggedInAuthorizationStrategy) {
            return
        }

        LOGGER.info('setting FullControlOnceLoggedInAuthorizationStrategy')

        def authorization = new FullControlOnceLoggedInAuthorizationStrategy()

        LOGGER.info('disabling anonymous read access')
        authorization.setAllowAnonymousRead(false)
        
        jenkins.setAuthorizationStrategy(authorization)

    }

    void addAdminUser() {

        // username = user id
        def username = 'admin'
        def securityRealm = jenkins.getSecurityRealm()
        def currentUserNames = securityRealm.getAllUsers().collect { it.id }
        if (username in currentUserNames) {
            LOGGER.info("${username} user already exists, not creating")
            return
        }

        LOGGER.info("${username} user not found, creating...")

        def password = 'admin'
        LOGGER.warning('Using hardcoded admin/admin password, if you want to use a random admin password instead see the comments in the groovy bootstrap script')
        // uncomment to use random password instead:
        //def password=UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ENGLISH)
        //LOGGER.warning("Random admin password: ${password}")

        def user = securityRealm.createAccount(username, password)
        user.save()

    }

    void disableCLIRemoting() {

        LOGGER.info('disabling CLI Remoting')

        jenkins.CLI.get().setEnabled(false)

    }

    void disableDeprecatedAgentProtocols() {

        def enabledAgentProtocols = jenkins.getAgentProtocols()
        LOGGER.info("current enabled agent protocols: ${enabledAgentProtocols}")

        def deprecatedProtocolsAsOfMarch2018 = [
            "JNLP-connect", 
            "JNLP2-connect", 
            "JNLP3-connect", 
            "CLI2-connect", 
            "CLI-connect"
            ]

        def newEnabledProtocols = enabledAgentProtocols.minus(deprecatedProtocolsAsOfMarch2018)

        def deprecatedProtocolsToDisable = enabledAgentProtocols.minus(newEnabledProtocols)
        LOGGER.info("disabling deprecated protocols: ${deprecatedProtocolsToDisable}")

        LOGGER.info("setting enabled agent protocols: ${newEnabledProtocols}")
        jenkins.setAgentProtocols(newEnabledProtocols)

    }

    void mitigateCSRF() {

        LOGGER.info('setting DefaultCrumbIssuer to mitigate CSRF attacks')

        jenkins.setCrumbIssuer(new DefaultCrumbIssuer(false))

    }

    void setMasterToSlaveSecurity() {

        // from SetupWizard.java
        jenkins.getInjector()
            .getInstance(AdminWhitelistRule.class)
            .setMasterKillSwitch(false)
        
    }

    void setWelcomeMessage() {

        jenkins.model.Jenkins.instance.setSystemMessage "Welcome to g0t4/jenkins-bootstrapped"
    
    }

}
