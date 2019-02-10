// 代码检出

node {

    stage('Get Code') {
        echo env.helloStr
        echo "test1"
        git credentialsId: 'e10fe825-e885-4ddf-bc96-30dbc30f94dd', url: 'https://github.com/JOHNKING123/DataSearcher.git'
    }

    stage('Maven build'){
        def packagePath = 'com.zhengcq:datasercher'
        withMaven(jdk:'jdk1.8', maven:'maven') {
            sh "mvn package -pl ${packagePath} -am -Dmaven.test.skip=true -e"
        }

    }


    stage('docker build'){
        sh "docker build -t datasearch ./datasercher"
    }

    stage("Restart app") {
        def ANSIBLE_PATH = '../get-deployment/ansible'
        dir(ANSIBLE_PATH) {
            sh "ansible-playbook -i hosts datasearch.yml"
        }
    }



}
