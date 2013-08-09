Summary: monkey is baseon zookeeper
Name:monkey
Version:2.0.1
Release:1
Vendor:Fast sky (fast_sky@sina.com)
License:Share
Group:Applications/Text
Source0:monkey.tar.gz
#BuildRoot:/Application/monkey

%description
monkey first to complie rpm

%prep
%setup -n %{name}

%build
make

%install
rm -rf $RPM_BUILD_ROOT
make install DESTDIR=$RPM_BUILD_ROOT

%clean
rm -rf $RPM_BUILD_DIR/%{name}
rm -rf $RPM_BUILD_ROOT
rm -f %{_bindir}/monkey
rm -f %{_bindir}/updown
#make uninstall DESTDIR=$RPM_BUILD_ROOT

%files
%defattr(-,root,root)
/Application/monkey
%{_bindir}

%changelog
* Tue Sep 21 2013 Fast sky (fast_sky@sina.com)
- Fast sky test it
