/*noderank,sv,based on LIN*/
set Nv;/*虚拟节点的集合*/
set Ns;/*物理节点的集合*/
set X{i in Nv};/*每个虚拟节点可映射的物理节点集合*/



param p{i in Nv};/*虚拟节点CPU*/
param sv{i in Nv,j in X[i]};/*虚拟节点i与物理节点j的相似度值*/

var A{i in Nv,k in Ns},binary;/*虚拟节点i映射到物理节点k上，0如果成功则为1，否则为0*/

minimize  slotsMin: sum{i in Nv}sum{j in X[i]}sv[i,j]*A[i,j]*p[i];


/*公式4*/
s.t. NodeAssignmentCon1{i in Nv}: sum{k in X[i]} A[i,k]=1;/*公式5*/
s.t. NodeAssignmentCon2{k in Ns}: sum{i in Nv} A[i,k]<=1;/*公式6*/
end;
