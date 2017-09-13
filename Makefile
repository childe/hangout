VERSION := 0.1.7
RELEASEPATH = release/$(FULLVERSION)

default: 2.4

all: 2.4 2.4 2.3 2.2 2.1 2.0 1.7 1.6 1.5

clean:
	rm -rf release

build:
	git checkout $(GITBRANCH)
	mkdir -p $(RELEASEPATH)
	mkdir -p $(RELEASEPATH)/bin
	mkdir -p $(RELEASEPATH)/lib
	mkdir -p $(RELEASEPATH)/vender
	cp example.yml $(RELEASEPATH)
	cp LICENSE $(RELEASEPATH)
	cp bin/hangout $(RELEASEPATH)/bin
	git rev-parse --short HEAD > $(RELEASEPATH)/VERSION
	mvn clean package
	mvn dependency:copy-dependencies
	cp target/hangout-$(VERSION).jar $(RELEASEPATH)/lib
	cp target/dependency/* $(RELEASEPATH)/vender
	sed -i '' 's/X.X.X/$(VERSION)/' $(RELEASEPATH)/bin/hangout
	tar -cf release/$(FULLVERSION).tar -C release $(FULLVERSION)
	git reset --hard

test:
	$(MAKE) build FULLVERSION=hangout-test GITBRANCH=$@ ESVERSION=2.4.4

dev:
	$(MAKE) build FULLVERSION=hangout-dev GITBRANCH=$@ ESVERSION=2.4.4

2.4:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=es2.X ESVERSION=2.4.4

2.3:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=es2.X ESVERSION=2.3.0

2.2:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=es2.X ESVERSION=2.2.0

2.1:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=es2.X ESVERSION=2.1.0

2.0:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=es2.X ESVERSION=2.0.0

1.7:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=es1.X ESVERSION=1.7.0

1.6:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=es1.X ESVERSION=1.6.0

1.5:
	$(MAKE) build FULLVERSION=hangout-$(VERSION)-ES$@ GITBRANCH=es1.X ESVERSION=1.5.0
