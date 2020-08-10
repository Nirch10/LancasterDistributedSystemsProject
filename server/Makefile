#
# Arguments
#
JEDIS_VERSION ?= 3.1.0
ORGJSON_VERSION ?= 20190722

#
# Directories
#
SRC_DIR ?= ./src
OUT_DIR ?= ./out
LIB_DIR ?= ./libs

#
# Tools
#
JAVAC  ?= javac
JFLAGS ?= -g -d $(OUT_DIR) -classpath $(OUT_DIR):"$(LIB_DIR)/*" -sourcepath $(SRC_DIR) 
DOCKER ?= docker

#
# Targets
#
.PHONY: all
all: libs $(OUT_DIR) ## Compile the Java server
	$(JAVAC) $(JFLAGS) $(SRC_DIR)/Server.java

$(OUT_DIR): ## Create the out/ directory
	mkdir -p $(OUT_DIR)

.PHONY: libs
libs: ## Retrieve required libraries
	wget -nc -P $(LIB_DIR) https://repo1.maven.org/maven2/redis/clients/jedis/$(JEDIS_VERSION)/jedis-$(JEDIS_VERSION).jar
	wget -nc -P $(LIB_DIR) https://repo1.maven.org/maven2/org/json/json/$(ORGJSON_VERSION)/json-$(ORGJSON_VERSION).jar

.PHONY: clean
clean: ## Remove the out/ directory and all the .class files
	$(RM) -Rfv $(OUT_DIR)