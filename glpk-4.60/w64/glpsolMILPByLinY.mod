set Nv;/*虚拟节点的集合*/
set Lv, within Nv cross Nv;/*虚拟链路的集合*/
set Ns;/*物理节点的集合*/
set Nss{k in Ns};/*物理节点k对应的链路的另外一个节点集合*/
set Nss1{k in Ns};/*物理节点k对应的链路的前驱节点集合*/
set Ls, within Ns cross Ns;/*物理链路集合*/
set X{i in Nv};/*每个虚拟节点可映射的物理节点集合*/
set SlotIndex;/*频谱槽索引集合*/
set MD;/*调制模式，MD=1,2,3,4,6,8,分别对应BPSK,QPSK,8QAM,16QAM,64QAM,256QAM*/
set Slot{(i,j) in Lv,a in SlotIndex,model in MD};/*从a到a+1,...,a+|bw/model|+G-1*/
set Slot1{(i,j) in Lv,a in SlotIndex,model in MD};/*从a+|bw/model|+G-1到a+|bw/model|+G-1+1,...,a*/

param c{k in Ns};/*物理节点的计算单位代价*/
param cs{(i,j) in Ls};/*物理链路的频谱槽单位代价，与长度成比例*/
param p{i in Nv};/*虚拟节点CPU*/
param pbw{(i,j) in Lv};/*虚拟链路带宽请求*/
param F;/*最大的频谱槽索引*/
param U{k in Ns};/*物理节点k的剩余cpu*/
param USlot{(i,j) in Ls,t in SlotIndex};/*物理链路频谱槽的状态，1表示空闲；0表示被占用*/
param d{(i,j) in Ls};/*物理链路的长度*/
param dis{m in MD};/*调制模式最大传输距离*/
param R;/*最大的整数*/
param G;/*隔离槽数量*/

var M{(i,j) in Lv,(m,n) in Ls,a in SlotIndex}, binary;/*二进制变量，如果虚拟网络链路(i,j)映射在链路(m,n)的起始索引a上，则为1，否则为0*/
var A{i in Nv,k in Ns},binary;/*虚拟节点i映射到物理节点k上，如果成功则为1，否则为0*/
var T{(i,j) in Lv,a in SlotIndex},binary;/*虚拟链路(i,j)映射到物理网络中的起始索引a上*/
var S{(i,j) in Lv,m in MD},binary;/*虚拟链路(i,j)选择的调制模式mod*/
var Z{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD},binary;/*虚拟链路映射到物理链路上，其起始频谱槽索引为a，调制模式为mod*/

/*minimize slotsMin: sum{i in Nv}sum{k in Ns}(c[k])*p[i]*A[i,k];*/
minimize slotsMin: sum{(i,j) in Lv}sum{(m,n) in Ls}sum{a in SlotIndex}sum{md in MD}cs[m,n]*((pbw[i,j]/(12.5*md)+1)+G)*Z[i,j,m,n,a,md]+sum{i in Nv}sum{k in Ns}c[k]*p[i]*A[i,k];

/*minimize slotsMin: sum{(i,j) in Lv}sum{(m,n) in Ls}sum{a in SlotIndex}M[i,j,m,n,a]+sum{(i,j) in Lv}sum{(m,n) in Ls}sum{a in SlotIndex}sum{md in MD}cs[m,n]*((pbw[i,j]/(12.5*md)+1)+G)*Z[i,j,m,n,a,md];*/
/*公式4*/
s.t. NodeAssignmentCon1{i in Nv}: sum{k in X[i]} A[i,k]=1;/*公式5*/
s.t. NodeAssignmentCon2{k in Ns}: sum{i in Nv} A[i,k]<=1;/*公式6*/
/*s.t. VLinkAssignmentCon1{(i,j) in Lv,k in Ns}: sum{a in SlotIndex}sum{m in Nss1[k]}M[i,j,m,k,a]-sum{a in SlotIndex}sum{n in Nss[k]}M[i,j,k,n,a]=A[j,k]-A[i,k];公式7*/

s.t. SpectrumAssignmentCon1{(i,j) in Lv}: sum{a in SlotIndex} T[i,j,a]=1;/*公式8*/
s.t. SpectrumAssignmentCon2{(i,j) in Lv,a in SlotIndex}: sum{(m,n) in Ls} M[i,j,m,n,a]>=T[i,j,a];/*公式9*/
s.t. SpectrumAssignmentCon3{(i,j) in Lv,a in SlotIndex}: sum{(m,n) in Ls} M[i,j,m,n,a]<=R*T[i,j,a];/*公式10*/
s.t. ModulationAssignmentCon1{(i,j) in Lv}: sum{md in MD} S[i,j,md]=1;/*公式11*/
s.t. ModulationAssignmentCon2{(i,j) in Lv}: (sum{(m,n) in Ls}sum{a in SlotIndex}(M[i,j,m,n,a]*d[m,n]))<=(sum{md in MD}(S[i,j,md]*dis[md]));/*公式12*/
s.t. NetResAssignmentCon1{k in Ns}: sum{i in Nv}p[i]*A[i,k]<=U[k];/*公式13/
/*s.t. NetResAssignmentCon1{k in Nv}: sum{i in Ns}U[i]*A[k,i]>=p[k];cxh增加公式13/
s.t. NetResAssignmentCon2{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: (pbw[i,j]/(12.5*md)+G)*Z[i,j,m,n,a,md]<=sum{t in Slot[i,j,a,md]}U[m,n,t];/*公式14*/
s.t. NetResAssignmentCon3{(m,n) in Ls,a in SlotIndex}: sum{(i,j) in Lv}sum{md in MD}sum{t in Slot1[i,j,a,md]}Z[i,j,m,n,t,md]<=USlot[m,n,a];/*公式15*/
s.t. NonlinearCon1{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: M[i,j,m,n,a]+S[i,j,md]-1<=Z[i,j,m,n,a,md];/*公式16*/
s.t. NonlinearCon2{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: M[i,j,m,n,a]+S[i,j,md]>=2*Z[i,j,m,n,a,md];/*公式17*/
/*s.t. cxhCon1{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: a+(pbw[i,j]/(12.5*md)+G-1)*Z[i,j,m,n,a,md]<=F;公式cxh补充2020-1-29表示分配的频谱槽不能大于最大的频谱槽索引*/
/*s.t. cxhCon2{(m,n) in Ls,a in SlotIndex,(i,j) in Lv,md in MD}: (pbw[i,j]/(12.5*md)+G-1)*Z[i,j,m,n,a,md]<=sum{t in Slot[i,j,a,md]}USlot[m,n,t];cxh补充2020-1-29，即分配的频谱槽应该是连续的*/
end;