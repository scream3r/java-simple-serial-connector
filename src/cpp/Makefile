VER = 2.8
MAKEARCH = $(MAKE) ARCH=$(ARCH) CROSS_COMPILE=$(CROSS_COMPILE)

all:
	$(MAKEARCH) VER=$(VER) -C _nix_based/
	mkdir -p target
	cp -f _nix_based/*.so target/

clean:
	rm -rf target
	$(MAKE) -C _nix_based/ clean	
