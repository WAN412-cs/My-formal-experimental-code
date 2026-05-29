set Nv;/*虚拟节点的集合*/
set Ns;/*虚拟节点对应的物理节点的集合*/
set A{u in Nv};

param cost{i in Nv,j in Ns};

var x{i in Nv,j in Ns},binary;

minimize costM:sum{i in Nv}sum{j in A[i]}cost[i,j]*x[i,j];
/*maximize costM:sum{i in Nv}sum{j in A[i]}cost[i,j]*x[i,j];*/

s.t. NodeAssignmentCon1{i in Nv}: sum{j in A[i]} x[i,j]=1;
s.t. NodeAssignmentCon2{i in Ns}: sum{j in Nv} x[j,i]<=1;

end;