# Below we start the master pod and service first.
# Then, we start slaves, which will connect through the 
# slave proxy. 
kubectl create -f ./spark-master-pod.yaml 
kubectl create -f ./spark-master-service.json 
kubectl create -f ./spark-slave-controller.json 
kubectl create -f ./spark-slave-service.json 
kubectl get pods,services
