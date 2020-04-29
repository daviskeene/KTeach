FROM openjdk:10
WORKDIR /
# Set up Google Auth Credentials
RUN echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] http://packages.cloud.google.com/apt cloud-sdk main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list && curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key --keyring /usr/share/keyrings/cloud.google.gpg  add - && apt-get update -y && apt-get install google-cloud-sdk -y
COPY / /KTeach
COPY build/google/poke-app-73a1c80da3ac.json credentials.json
ENV GOOGLE_APPLICATION_CREDENTIALS=/credentials.json
RUN gcloud auth activate-service-account --key-file=credentials.json
# Set up kotlinc
RUN apt-get install unzip
RUN apt-get install zip
RUN apt-get install sed
RUN cd /usr/lib && \
    wget -q https://github.com/JetBrains/kotlin/releases/download/v1.3.61/kotlin-compiler-1.3.61.zip && \
    unzip kotlin-compiler-*.zip && \
    rm kotlin-compiler-*.zip && \
    rm -f kotlinc/bin/*.bat
ENV PATH $PATH:/usr/lib/kotlinc/bin
WORKDIR /KTeach
# Run the app
CMD ["java", "-jar", "build/libs/hello-1.0-SNAPSHOT-all.jar"]