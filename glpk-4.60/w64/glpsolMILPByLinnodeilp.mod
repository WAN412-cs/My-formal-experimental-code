set Nv;/*虚拟节点的集合*/
set Ns;/*物理节点的集合*/
set X{i in Nv};/*每个虚拟节点可映射的物理节点集合*/

param c{k in Ns};/*物理节点的计算单位代价*/
param p{i in Nv};/*虚拟节点CPU*/



var A{i in Nv,k in Ns},binary;/*虚拟节点i映射到物理节点k上，如果成功则为1，否则为0*/


minimize slotsMin: sum{i in Nv}sum{k in Ns}(c[k])*p[i]*A[i,k];


/*公式4*/
s.t. NodeAssignmentCon1{i in Nv}: sum{k in X[i]} A[i,k]=1;/*公式5*/
s.t. NodeAssignmentCon2{k in Ns}: sum{i in Nv} A[i,k]<=1;/*公式6*/

/*s.t. cxhCon2{(m,n) in Ls,a in SlotIndex,(i,j) in Lv,md in MD}: (pbw[i,j]/(12.5*md)+G-1)*Z[i,j,m,n,a,md]<=sum{t in Slot[i,j,a,md]}USlot[m,n,t];cxh补充2020-1-29，即分配的频谱槽应该是连续的*/
end;