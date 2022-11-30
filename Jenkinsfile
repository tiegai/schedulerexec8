#!groovy
@Library([
	'cicd-pipeline',
	'cop-pipeline-configuration@c4c-ncp_springboot_template_Yizhi',
	'cop-pipeline-step'
]) _ // Why is there a trailing underscore `_`? https://stackoverflow.com/a/48798886

def config = [
	/**
	 * Replace with the shared configurations of your choice.
	 * Your project inherits and merges all these shared configurations. Find each of them at:
	 * https://github.com/nike-cop-pipeline/cop-pipeline-configuration/tree/c4c-ncp/resources
	 */
	profile: [
		debug	: "true",	// prints complete final config in console during pipeline execution
		build	: 'team/maui/c4c/ec2/build.groovy',
		deploy	: 'team/maui/c4c/ec2/deploy.groovy',
		tags	: 'team/maui/c4c/ec2/tags.groovy',
	],
	
	// Configurations beyond this line will override their counterparts inherited from above.
	
	buildFlow: [
		SFX_RESOURCES_UPDATE: ["SignalFXResources"],
		PULL_REQUEST		: ["Build", "Compile", "Extract Version", "Scan", "ScanAtSource", "Quality Gate", "PRA Comment"], // PULL_REQUEST name should match and it is only mandatory Name in build flow.
		TEST_DEPLOY			: ["Build", "Compile", "Extract Version", "Scan", "ScanAtSource", "Quality Gate", "AMI", "SignalFxTest"],
		PERF_DEPLOY			: ["Build", "Compile", "Extract Version", "Scan", "ScanAtSource", "Quality Gate", "AMI", "AMI-Share", "SignalFxTest"],
		PROD_DEPLOY			: ["Build", "Compile", "Extract Version", "Scan", "ScanAtSource", "Quality Gate", "AMI", "Smart Share", "SignalFxProd"],
	],

	branchMatcher: [
		PULL_REQUEST	: ['^(?!master$).*$'],
		PERF_DEPLOY		: ['^(?!master$).*$'],
		TEST_DEPLOY		: ['master'],
		PROD_DEPLOY		: ['master'],
	],

	// Useful for mitigating contention when trying to lock Gradle journal cache
	// https://confluence.nike.com/display/GCENG/C4C+CDS+Migration+-+Lessons+Learned#C4CCDSMigrationLessonsLearned-Timeoutwaitingtolockjournalcache(/tmp/jenkins/workspace/S_V1_notificationsmsproc_develop/.gradle/caches/journal-1).ItiscurrentlyinusebyanotherGradleinstance.
	cache: [
		strategy	: 'mountAsDockerVolume',
		isolation	: 'pipeline', // https://nikedigital.slack.com/archives/C037WGKUWRZ/p1649666711261889?thread_ts=1649665633.876249&cid=C037WGKUWRZ
		tool		: 'gradle'
	],

	build: [
		cmd: "./gradlew clean build --parallel --daemon --build-cache",
	],

	package: [
		requires: ['nike-springboot-support', 'nike-signalfx-collectd', 'nike-use-openjdk-11']
	],

	deploymentEnvironment: [
		test: [
			deployOrder: 10, // optional, learn more at https://pipelines.auto.nikecloud.com/concepts/defining-deployment-order/#how-to-define-the-deployment-order
			deploy: [
				instanceType	: 'm5.large',
				maxSize			: 1,
				minSize			: 1,
				desiredCapacity	: 1,
			],
			infrastructure: [
				loadBalancer: [
					loadBalancerTargetGroupArn	: 'arn:aws-cn:elasticloadbalancing:cn-northwest-1:128123422106:targetgroup/springboottemplate-tg/fbfe3ca90e1ee57b',
					listenerArn					: 'arn:aws-cn:elasticloadbalancing:cn-northwest-1:128123422106:listener/app/springboottemplate-ALB/5451d7e91d7f8781/4af83b150b585ced'
				]
			],
		],

		prod: [
			deployOrder: 30, // optional, learn more at https://pipelines.auto.nikecloud.com/concepts/defining-deployment-order/#how-to-define-the-deployment-order
			deploy: [
				instanceType	: 'm5.large',
				maxSize			: 24,
				minSize			: 1,
				desiredCapacity	: 1,
				// scaling configs below are encouraged but optional
				// https://github.com/nike-cop-pipeline/cop-pipeline-step/blob/main/resources/com/nike/acid/pipeline/step/asgWithOptionalRouting.template.yaml
				includeDefaultCPUScalingPolicy : true,
				scaleUpCPUThreshold		: 50,
				scaleDownCPUThreshold	: 20,
				scaleUpAdjustment		: 9,
				scaleDownAdjustment		: -3,
			],
			infrastructure: [
				loadBalancer: [
					loadBalancerTargetGroupArn	: 'arn:aws-cn:elasticloadbalancing:cn-northwest-1:128277374507:targetgroup/springboottemplate-tg/fbfe3ca90e1ee57b',
					listenerArn					: 'arn:aws-cn:elasticloadbalancing:cn-northwest-1:128277374507:listener/app/springboottemplate-ALB/5451d7e91d7f8781/4af83b150b585ced'
				]
			]
		],
	],
]

def params = [
	envVars: env,
]

node {
	// use the build number as part of the deployment version number
	def scmVars = checkout scm
	String appName = readProperties(file: 'gradle.properties').artifactId
	String version = readProperties(file: 'gradle.properties').version + "." + env.BUILD_ID
	branchName = env.BRANCH_NAME
	
	params.put("scmVars", scmVars)
	params.put("appName", appName)
	params.put("version", version)
	params.put("teamName", "GC-MarTech")
	
	config = mergeConfiguration(config, params)
}

ec2BlueGreenDeployPipeline(config)
