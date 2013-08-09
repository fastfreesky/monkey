all:
	@echo `date`

DESTDIR=
DESTDIRBASE=${DESTDIR}/Application/monkey
STATEDIR=${DESTDIRBASE}/state
JARDIR=${DESTDIRBASE}/jar
CONFDIR=${DESTDIRBASE}/conf

BINDIR=${DESTDIR}/usr/bin
LINK_MONKEY=${BINDIR}/monkey
LINK_UPDOWN=${BINDIR}/updown

install:
	rm -f ${LINK_UPDOWN}
	rm -f ${LINK_MONKEY}
	mkdir -p ${DESTDIRBASE}
	cp ./* -r ${DESTDIRBASE}
	mkdir -p ${BINDIR}
	cp ${CONFDIR}/updown ${LINK_UPDOWN}
	cp ${CONFDIR}/monkey ${LINK_MONKEY}
#	ln -sf ${CONFDIR}/updown ${LINK_UPDOWN}
#	ln -sf ${CONFDIR}/monkey ${LINK_MONKEY}
	chmod -R 777 ${DESTDIRBASE}
	
uninstall:
	rm -rf ${DESTDIRBASE}
	rm -f ${LINK_UPDOWN}
	rm -f ${LINK_MONKEY}
