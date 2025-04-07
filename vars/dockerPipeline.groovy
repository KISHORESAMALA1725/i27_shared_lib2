// Import the Docker class
import com.i27academy.builds.Docker

// Define methods outside the pipeline block
def buildApp() {
    echo "***** Building the Application *****"
    sh "mvn clean package -DskipTest=true"
    archiveArtifacts 'target/*.jar'
}

def dockerBuildPush() {
    echo "****** Building Docker image *******"
    sh "cp ${WORKSPACE}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} ./.cicd"
    sh "docker build --no-cache --build-arg JAR_SOURCE=i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} -t ${DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT} ./.cicd"
    withCredentials([usernamePassword(credentialsId: 'kishoresamala84_docker_creds', usernameVariable: 'DOCKER_CREDS_USR', passwordVariable: 'DOCKER_CREDS_PSW')]) {
        sh "docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}"
    }
    sh "docker push ${DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
}

def imageValidation() {
    echo '***** Attempting to pull the Docker image *****'
    try {
        sh "docker pull ${env.DOCKER_HUB}/${env.APPLICATION_NAME}:${GIT_COMMIT}"
        echo '***** Docker Image Pulled successfully *****'
    } catch (Exception e) {
        echo "OOOPS!!! Docker Image with this tag not found, so building the image now..."
        buildApp()
        dockerBuildPush()
    }
}

def deployToDocker(envDeploy, hostPort, contPort) {
    echo "***** Deploying to $envDeploy environment *****"
    withCredentials([usernamePassword(credentialsId: 'john_docker_vm_passwd', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
        script {
            // Add dev_ip as part of the parameters
            def dev_ip = pipelineParams.dev_ip  // Make sure dev_ip is passed as a parameter
            try {
                sh "sshpass
