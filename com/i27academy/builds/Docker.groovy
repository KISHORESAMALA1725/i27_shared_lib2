package com.i27academy.builds;

class Docker {
    def jenkins
    Docker(jenkins){
    this.jenkins = jenkins
    }

    def buildApp(appName) {
        jenkins.sh """
            echo "***** Building the Application *****"
            mvn clean package -DskipTest=true
            archiveArtifacts 'target/*.jar' 
            """   
    }
}