/*改动：14,43,45,53,57,58*/
set Nv;/*虚拟节点的集合*/
set Lv, within Nv cross Nv;/*虚拟链路的集合*/
set Ns;/*物理节点的集合*/
set Nsj{q in Ns};/*物理节点q对应的链路的前驱节点集合*/
set Nsb{q in Ns};/*物理节点q对应的链路的后续节点集合*/
set Ls, within Ns cross Ns;/*物理链路集合*/
set T{i in Nv};/*每个虚拟节点i可映射的物理节点集合*/
set SlotIndex;/*频谱槽索引集合*/
set MD;/*调制模式，MD=1,2,3,4,6,8,分别对应BPSK,QPSK,8QAM,16QAM,64QAM,256QAM*/
set Slotx{(i,j) in Lv,a in SlotIndex,model in MD};/*从a到a+1,...,a+|bw/model|+G-1*/
set Sloty{(i,j) in Lv,a in SlotIndex,model in MD};/*从a-|bw/model|+G-1到a*/

set NLs{q in Ns}, within Ns cross Ns;/*物理节点q的连接的物理链路集合*/

param p{i in Nv};/*虚拟节点CPU*/
param bw{(i,j) in Lv};/*虚拟链路带宽请求*/
param f;/*最大的频谱槽索引,单根光纤链路的频谱槽总量*/
param u{q in Ns};/*物理节点q的剩余cpu*/
param USlot{(i,j) in Ls,t in SlotIndex};/*物理链路频谱槽的状态，1表示空闲；0表示被占用*/
param l{(i,j) in Ls};/*物理链路的长度*/
param N{(i,j) in Lv,m in MD};/*在调制模式m下，分配给虚拟链路(i,j)的频谱槽数量*/
param dm{m in MD};/*调制模式最大传输距离*/
param G;/*隔离槽数量*/
param W;/*W=12.5Gb/s*/
param R;/*一个大的整数常数*/

param K{j in Ns};/*物理节点j是否被激活的状态，1表示未被激活；0表示被激活*/
param p1;
param p2;
param p3;
param p4;
param p5;


var D{(i,j) in Lv,(m,n) in Ls,a in SlotIndex},binary;/*二进制变量，如果虚拟网络链路(i,j)映射（经过？）在链路(m,n)的起始索引a上，则为1，否则为0*/
var F{i in Nv,q in Ns},binary;/*虚拟节点i映射到物理节点q上，如果成功则为1，否则为0*/
var S{(i,j) in Lv,a in SlotIndex},binary;/*虚拟链路(i,j)映射到物理网络中的频谱槽索引a上*/
var M{(i,j) in Lv,m in MD},binary;/*虚拟链路(i,j)选择的调制模式m*/
var V{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD},binary;/*虚拟链路映射到物理链路上，其起始频谱槽索引为a，调制模式为md*/

var A{q in Ns},binary;/*物理节点q是否被经过,1表示经过,0表示未经过*/
var Z{(i,j) in Lv,(m,n) in Ls},binary;/*虚拟链路(i,j)是否经过物理链路(m,n),1表示映射,0表示未映射*/

minimize slotANDenergyMin:0.5*(sum{(i,j) in Lv}sum{(m,n) in Ls}sum{a in SlotIndex}sum{md in MD}(V[i,j,m,n,a,md]*N[i,j,md]))+0.5*(sum{q in Ns}(((p1+p2+p3)*K[q]*A[q])+sum{i in Nv}(p4*p[i]*F[i,q])+sum{(m,n) in NLs[q]}sum{(i,j) in Lv}(p5*Z[i,j,m,n]*bw[i,j])));


/*节点映射约束*/
s.t. NodeAssignmentCon1{i in Nv}: sum{q in T[i]} F[i,q]=1;
s.t. NodeAssignmentCon2{q in Ns}: sum{i in Nv} F[i,q]<=1;
s.t. NodeAssignmentCon3{i in Nv,q in Ns}:p[i]*F[i,q]<=u[q];

s.t. NodeAssignmentCon4{i in Nv,q in Ns}:A[q]-F[i,q]>=0;/*物理节点被映射一定被经过*/
/*链路映射二进制流约束*/
s.t. VLinkAssignmentCon1{(i,j) in Lv,q in Ns}: sum{a in SlotIndex}sum{m in Nsj[q]}D[i,j,m,q,a]-sum{a in SlotIndex}sum{n in Nsb[q]}D[i,j,q,n,a]=F[j,q]-F[i,q];              
s.t. VLinkAssignmentCon2{(i,j) in Lv ,(m,n) in Ls,a in SlotIndex}: D[i,j,m,n,a] <=Z[i,j,m,n];/*D为1时Z为1*/
s.t. VLinkAssignmentCon3{(i,j) in Lv ,(m,n) in Ls}:Z[i,j,m,n]<=A[m]+A[n];/*被经过的链路的端点被经过(可以试着分开）*/


/*调制模式约束*/
s.t. ModulationAssignmentCon1{(i,j) in Lv}: sum{md in MD} M[i,j,md]=1;
s.t. ModulationAssignmentCon2{(i,j) in Lv}: (sum{(m,n) in Ls}sum{a in SlotIndex}(D[i,j,m,n,a]*l[m,n]))<=(sum{md in MD}(M[i,j,md]*dm[md]));
/*频谱槽分配约束*/
s.t. SpectrumAssignmentCon1{(i,j) in Lv}: sum{a in SlotIndex} S[i,j,a]=1;
s.t. SpectrumAssignmentCon2{(i,j) in Lv,a in SlotIndex}: sum{(m,n) in Ls} D[i,j,m,n,a]>=S[i,j,a];
s.t. SpectrumAssignmentCon3{(i,j) in Lv,a in SlotIndex}: sum{(m,n) in Ls} D[i,j,m,n,a]<=R*S[i,j,a];

s.t. SpectrumAssignmentCon4{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: a+N[i,j,md]*V[i,j,m,n,a,md]<=f;
s.t. SpectrumAssignmentCon5{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: N[i,j,md]*V[i,j,m,n,a,md]<=sum{d in Slotx[i,j,a,md]}USlot[m,n,d];
s.t. SpectrumAssignmentCon6{(m,n) in Ls,a in SlotIndex}: sum{(i,j) in Lv}sum{md in MD}sum{d in Sloty[i,j,a,md]}V[i,j,m,n,d,md]<=USlot[m,n,a];

/*线性转换*/
s.t. NonlinearCon1{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: D[i,j,m,n,a]+M[i,j,md]-1<=V[i,j,m,n,a,md];
s.t. NonlinearCon2{(i,j) in Lv,(m,n) in Ls,a in SlotIndex,md in MD}: D[i,j,m,n,a]+M[i,j,md]>=2*V[i,j,m,n,a,md];



end;
