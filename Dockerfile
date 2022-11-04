# === STAGE 1 ================================================================ #
# Build the uberjar
# ============================================================================ #
# https://hub.docker.com/_/clojure
FROM clojure:tools-deps-1.11.1.1189-jammy AS builder

LABEL maintainer="giacomo@giacomodebidda.com"

ARG APP_DIR=/usr/src/app
RUN if [ -z "${APP_DIR}" ] ; then echo "The APP_DIR argument is missing!" ; exit 1; fi

ARG APP_NAME
RUN if [ -z "${APP_NAME}" ] ; then echo "The APP_NAME argument is missing!" ; exit 1; fi

ARG APP_VERSION
RUN if [ -z "${APP_VERSION}" ] ; then echo "The APP_VERSION argument is missing!" ; exit 1; fi

RUN mkdir -p ${APP_DIR}

WORKDIR ${APP_DIR}

# I think that resources (i.e. assets) and source code change frequently, while
# build scripts and dependencies change less frequently. That's why I decided
# to define the docker layers in this order.
COPY build.clj ${APP_DIR}/
COPY deps.edn ${APP_DIR}/
COPY resources ${APP_DIR}/resources
COPY src ${APP_DIR}/src

RUN clojure -T:build uber

# === STAGE 2 ================================================================ #
# Copy the uberjar built at stage 1 and run it as a non-priviled user
# ============================================================================ #
# https://github.com/GoogleContainerTools/distroless
FROM gcr.io/distroless/java17-debian11:nonroot

# Note: Distroless images are minimal and lack shell access. This means you
# can't use RUN instructions here. If you need shell access, you can use
# Distroless images that have the :debug tag.
# https://github.com/GoogleContainerTools/distroless#debug-images

# Each ARG goes out of scope at the end of the build stage where it was
# defined. That's why we have to repeat it here in this stage.
# To use an arg in multiple stages, EACH STAGE must include the ARG instruction.
# https://docs.docker.com/engine/reference/builder/#scope
# We also need to re-initialize EACH ARG to its default value (if it has one).
ARG APP_DIR=/usr/src/app
ARG APP_NAME
ARG APP_VERSION

WORKDIR /app

COPY --from=builder "${APP_DIR}/target/${APP_NAME}-${APP_VERSION}-standalone.jar" ./main.jar

CMD [ "main.jar" ]
