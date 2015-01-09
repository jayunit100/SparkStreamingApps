
for SERVICES in kube-proxy kubelet docker; do      systemctl restart docker;     systemctl enable docker;     systemctl status -l docker ; done

for serv  in etcd kube-apiserver kube-controller-manager kube-scheduler; do systemctl restart kube-scheduler; systemctl enable kube-scheduler; systemctl status kube-scheduler; done
