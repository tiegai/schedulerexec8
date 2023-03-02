#!groovy
@Library(['cop-pipeline-bootstrap', 'cop-pipeline-step']) _
loadPipelines('stable/v1.4.x', 'master')

enableLogger()

def cloudformationTemplatePath  = './cloudformation/ecs-service-deploy-template.yaml'



Map accountSettings = [
    test: [
        accountId        : '128123422106',
        securityGroups   : 'sg-0acc524825338bd51',
        privateSubnets   : 'subnet-0075a560decfbdc3f,subnet-066e0ec7e4787084e,subnet-02fbfc88a1e993db6',
        vpcId            : 'vpc-083124926037',
        awsRole          : 'gc-cds-jenkins',
        awsRegion        : 'cn-northwest-1',
        slackChannelName : '#gcncp-jenkins-auto-notifications',
        bmxAgentLabel    : 'ec2-ondemand-agent-cn',
        nikeTagGuid      : '8e339711-209e-4205-b792-02db90a2a934'
    ]
]

def account     = accountSettings.test

Map ecsServiceSettings = [
    'ClusterName'              : 'onencp-test-cluster',
    'TaskCpu'                  : '2048',
    'AppContainerCpu'          : '2048',
    'AppContainerMemory'       : '4096',
    'TaskMemory'               : '4096',
    'ContainerSecurityGroupId' : "${account.securityGroups}",
    'ContainerSubnetId'        : "${account.privateSubnets}",
    'ContainerPort'            : 8080,
    'HealthCheckPort'          : 8080,
    'ListenerRulePriority'     : 2,
    'ContainerDesiredCount'    : 1,
    'UseSplunkTaskDriver'      : 'true',
    'ServiceRoleName'          : 'arn:aws-cn:iam::128123422106:role/gc-cds-jenkins',
    'TaskExecutionRoleArn'     : 'arn:aws-cn:iam::128123422106:role/gc-ncp-memberunlock-ecs',
    'Environment'              : 'test',
]

def tags        = [
    'classification'        : 'Bronze',
    'costcenter'            : '161961',
    'email'                 : 'gc-marketing@nike.com',
    'nike_ca_qma_url'       : 'https://qma.auto.nikecloud.com/candidate/details/3c528dbe-98bb-452b-baca-5cad16f10a51',
    'nike-application'      : 'onencp',
    'nike-requestor'        : 'andrew.xiang@nike.com',
    'owner'                 : 'danny.zhang',
    'nike-department'       : 'gc marketing technology',
    'nike-domain'           : 'consumer engagement',
    'nike-distributionlist' : 'Lst-GT.GC-MarTech@nike.com',
    'nike-owner'            : 'Danny.Zhang@nike.com',
    'nike-tagguid'          : account.nikeTagGuid,
]

def pra         = [
    cerberusEnv     : "china-v2",
    sdbPath         : "shared/notification/credentials",
    userNameKey     : "gc-ncp-maui-pipelineuser",
    passwordKey     : "gc-ncp-maui-pipelinepassword",
]

def cloudRed    = [
    tagGuid         : account.nikeTagGuid,
    region          : account.awsRegion,
    env             : 'prod',
]


Map splunk = [:]


def buildFlow = [
    PULL_REQUEST        : ['Run Tests', 'QMA', 'Post PRA Comment'],
    DEPLOY_TO_TEST      : ['Build', 'Compile', 'Local Test', 'Containerize']
]

def deployFlow = [
    DEPLOY_TO_TEST      : ['Deploy', 'Publish To ECR'],
]

def branchMatcher = [
    DEPLOY_TO_TEST      : ['master'],
]

def qma = [
    configFile  : './quality-config.yaml',
]

def notify = [
    slack       : [
        onCondition : ['Build Start', 'Failure', 'Success', 'Unstable'],
        channel     : "${account.slackChannelName}",
    ]
]

def cache = [
    strategy    : 'mountAsDockerVolume',
    isolation   : 'pipeline',
    tool        : 'gradle',
]

def build = [
    image       : 'gradle:jdk11-focal',
    cmd         : './gradlew clean build --parallel --daemon --build-cache && chmod +x ./docker-entrypoint.sh && git log -3 > ./build/git.log',
    artifacts   : ['build/libs/'],
    cache       : [
        tool        : 'gradle',
    ]
]

def localTest = [
    image               : 'gradle:jdk11-focal',
    cmd                 : './gradlew integrationTest',
    archives            : ['build/reports/'],
]

def twistlock = [
    cerberusEnv         : "china-v2",
    action              : 'scan',
    twistlockSdb        : 'shared/notification/credentials',
    reportDir           : 'build/reports/twistlock',
    useQmaQualityGate   : true,
]



def config;


node {
    /* if (params.Flow == 'RELEASE') {
        splunk = withCerberus.readSecrets(
            env         : 'china-v2',
            sdbPath     : 'shared/notification/credentials',
        )
    } else {
        splunk = withCerberus.readSecrets(
            env         : 'china-v2',
            sdbPath     : 'shared/notification/credentials',
        )
    } */

    checkout scm
    withGit(credentialsId : 'GHEC') {
        sh "git pull"
    }

    def props                   = readProperties file: './gradle.properties'


    def artifactId              = props['artifactId']

    def customBuildParameters = [
        string(
            name            : 'NIKE_REQUESTOR',
            defaultValue    : 'andrew.xiang@nike.com',
            description     : 'AWS Resource Requstor',
        ),
        string(
            name            : 'ONENCP_SERVICE',
            defaultValue    : "${artifactId}",
            description     : "For service name check, Don't change me!",
        )

    ]

    def serviceName             = artifactId
    assert serviceName?.trim() && (! 'null'.equals(serviceName))

    def elbStackName            = "onencp-${serviceName}-ELB"
    def stackName               = "onencp-${serviceName}-TASK"
    def teamName                = "onencp"
    def serviceLable            = "onencp-${serviceName}"
    def twistlockProjectName    = "${serviceLable}"
    def imageName               = "${teamName}/${serviceName}"
    def imageFullName           = "${imageName}:${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    def loadBalancerRLable      = "app/onencp-internal-${serviceName}-alb/afb09e97b71572bf"
    String imageTag             = (params?.IMAGE_TAG ?:
                                   "${env.BRANCH_NAME}-${env.BUILD_NUMBER}".replaceAll("[^A-Za-z0-9]+", "-"))


    def container = [
        name                    : "${serviceName}",
        group                   : "${teamName}",
        imageName               : "${imageName}",
        releaseTag              : "${imageTag}",
    ]


    config = [
        customBuildParameters: customBuildParameters,
        usePraDispatch       : false,
        buildFlow            : buildFlow,
        branchMatcher        : branchMatcher,
        qma                  : qma,
        pra                  : pra,
        notify               : notify,
        cache                : cache,
        build                : build,
        localTest            : localTest,
        container            : container,
        tags                 : [
            'Name'              : "${serviceLable}",
            'nike-requestor'    : params.CUSTOM_NIKE_REQUESTOR,
        ] + tags,
        twistlock            : [
            twistlockScanTarget : "${teamName}/${serviceName}:${BRANCH_NAME}-${BUILD_ID}",
            twistlockProjectName: "${twistlockProjectName}",
            twistlockProjectId  : "${twistlockProjectName}",
        ] + twistlock,
        deploymentEnvironment: [
            test : [
                deployFlow      : deployFlow,
                cloudEnvironment: "test",
                deploy          : [
                    cloudFormationTemplate      : cloudformationTemplatePath,
                    parameters                  : [
                        'SplunkToken'                   : "af13b2c8-f4e4-42e1-a957-5742b55c7a91",
                        'SplunkIndex'                   : "np-dockerlogs",
                        'SplunkUrl'                     : "https://gcsplunk-hec.nike.com",
                        'SplunkApplicationName'         : "${serviceLable}",
                        'SplunkApplicationName'         : "${serviceLable}",
                        'ContainerDeployTag'            : "${imageTag}",
                        'ApplicationGroup'              : "${teamName}",
                        'ContainerName'                 : "${serviceName}",
                        'Domain'                        : "${teamName}",
                        'ServiceName'                   : "${serviceName}",
                        'ElbStackName'                  : "${elbStackName}",
                        'LoadBalancerResourceLabel'     : "${loadBalancerRLable}",
                    ] + ecsServiceSettings,
                    awsRole                             : "${account.awsRole}",
                    accountId                           : "${account.accountId}",
                    stackName                           : "${stackName}",
                    region                              : "${account.awsRegion}",
                    pollInterval                        : 15000,
                    useMultibranchCompatibleServiceName : true,
                ],
                tags                            : [
                    'nike-environment'                  : 'test',
                ],
            ],
        ],
    ]
}

ecsDeployPipeline(config)