%define name optimo-history-api
Name: %{name}
Version: %{?buildnum}%{!?buildnum:0}
Release: 1%{?dist}

%define buildroot %(mktemp -ud %{_tmppath}/%{name}-%{version}-%{release}-XXXXXX)

Summary: optimo-history-api

Group: Installation Script
License: internal BBC use only
Source: %{name}.tar.gz
BuildRoot: %{buildroot}
Requires: cloud-httpd24-ssl-services-devs
Requires: java-1.8.0-openjdk
Requires: ibl-sysadmin
Requires: cps-collectd
Requires: cps-filebeat
Requires: jq
AutoReqProv: no

%description


%prep
%setup -q -c -n %{name}

%build

%pre
getent group optimo-history-api >/dev/null || groupadd -r optimo-history-api
getent passwd optimo-history-api >/dev/null || useradd -r -g optimo-history-api -G optimo-history-api -d / -s /sbin/nologin -c "optimo-history-api" optimo-history-api

%install
mkdir -p %{buildroot}/usr/lib/optimo-history-api
cp -r ./ %{buildroot}/usr/lib/optimo-history-api
mkdir -p %{buildroot}/var/log/optimo-history-api
mkdir -p %{buildroot}/etc/bake-scripts/optimo-history-api/
mv %{buildroot}/usr/lib/optimo-history-api/bake-scripts/* %{buildroot}/etc/bake-scripts/optimo-history-api/.
rm -fr %{buildroot}/usr/lib/optimo-history-api/bake-scripts

%post
systemctl enable /usr/lib/optimo-history-api/optimo-history-api.service

%clean
rm -rf %{buildroot}

%files
%defattr(644, optimo-history-api, optimo-history-api, 755)
/usr/lib/optimo-history-api
/var/log/optimo-history-api
%attr(755, root, root) /etc/bake-scripts/optimo-history-api/*
%attr(755, optimo-history-api, optimo-history-api) /usr/lib/optimo-history-api/target/scala-2.11/optimo-history-api.jar
