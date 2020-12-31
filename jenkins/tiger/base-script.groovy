// 代码检出

node {

    def VERSION = env.VERSION ?: 'latest'
    def SERVICE = env.SERVICE ?: 'unknown-service'
    def BRANCH = env.BRANCH ?: 'master'
    def TARGET_ENV = env.TARGET_ENV ?: 'uat'

    def BUILD_ACTION = env.BUILD_ACTION ?: 'build,restart'
    def DO_BUILD = BUILD_ACTION.contains("build")
    def DO_RESTART = BUILD_ACTION.contains("restart")
    def DO_PUSH = BUILD_ACTION.contains("push")

    def SERVICE_DEF = [

            'tg-config': [path: 'tg-config', project: 'tg-config'],
            'tg-discovery': [path: 'tg-discovery', project: 'tg-discovery'],
            'tg-client1': [path: 'tg-client1', project: 'tg-client1'],
            'tg-client2': [path: 'tg-client2', project: 'tg-client2'],
            'gateway': [path: 'tg-gateway', project: 'tg-gateway']
    ]


    stage('Get Code') {
        echo env.helloStr
        echo "test1"
        git credentialsId: 'github', url: 'https://github.com/JOHNKING123/Tiger.git',branch: BRANCH
    }

    stage('Maven build'){
        //def packagePath = 'com.zhengcq:datasercher'
        def packagePath = SERVICE_DEF[SERVICE]["path"]
        echo packagePath
        withMaven(jdk:'jdk1.8', maven:'maven') {
            sh "mvn package -pl ${packagePath} -am -Dmaven.test.skip=true -e"
        }

    }

    docker.withRegistry('', 'dockerhub') {
        stage("Build Docker") {
            def project = SERVICE_DEF[SERVICE]["project"]
            def packagePath = SERVICE_DEF[SERVICE]["path"]
            def strImgTagId = "johnking123/${SERVICE}:${VERSION}"
            def strJarFile = "./target/${project}-1.0-SNAPSHOT.jar"
            sh "docker build -t ${strImgTagId} --build-arg JAR_FILE=${strJarFile} ${packagePath}"
            if(DO_PUSH) {
                sh "docker push ${strImgTagId}"
            }

        }
    }


    // 调用ansible启动服务
    if(DO_RESTART) {
        // 调用ansible启动服务
        stage("Restart app") {
            def ANSIBLE_PATH = '../get-development/ansible'
            def ansible_tag = "${SERVICE}-run"
            def envVar = "${SERVICE}-${TARGET_ENV}"
            dir(ANSIBLE_PATH) {
                sh "ansible-playbook -i env/${envVar} service-tiger.yml"
            }
        }
    }



}
