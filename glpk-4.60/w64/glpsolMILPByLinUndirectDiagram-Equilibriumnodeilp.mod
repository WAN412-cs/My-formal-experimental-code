set Nv;/*虚拟节点的集合*/
set Ns;/*物理节点的集合*/
set X{i in Nv};/*每个虚拟节点可映射的物理节点集合*/


param costCPU{k in Ns,i in Nv};/*物理节点的计算单位代价*/

var A{i in Nv,k in Ns},binary;/*虚拟节点i映射到物理节点k上，如果成功则为1，否则为0*/



        s.t. NodeAssignmentCon1{i in Nv}: sum{k in X[i]} A[i,k]=1;/*公式5*/
        s.t. NodeAssignmentCon2{k in Ns}: sum{i in Nv} A[i,k]<=1;/*公式6*/
minimize slotsMin:sum{i in Nv}sum{k in X[i]}costCPU[k,i]*A[i,k];


