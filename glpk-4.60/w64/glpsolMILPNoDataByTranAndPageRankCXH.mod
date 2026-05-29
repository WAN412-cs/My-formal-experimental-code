set MSet;/*索引集合，{0,1,...,M-1}*/
set D;/*虚拟链路集合*/
set Path;/*路径p的集合*/
set P{d in D};/*虚拟链路d的路径p集合*/
set Nv;/*虚拟节点的集合*/
set F;/*服务器节点（facility nodes）的集合*/
set Na;/*辅助图的节点集合，Na=F并Nv并网络节点*/
set A{u in Nv}, within Na cross Na;/*与虚拟节点u的辅助边的集合*/
set Afa{u in F}, within Na cross Na;/*与物理节点（facility nodes）u的辅助边的集合*/
set FS{d in D,p in Path};/*路径p上可能的起始频谱槽索引集合*/
set Ef, within Na cross Na;/*与服务器节点的辅助边的集合*/
set Af{u in Nv};/*每个虚拟节点可映射的节点集合*/

set Elink, within Na cross Na;/*链路集合*/
set PathByLink{(i,j) in Elink};/*通过链路(i,j)的路径集合*/


/*set Du{u in Nv};具有虚拟节点u的虚拟链路集合*/
set DNo{d in D};/*除去d的*/

param NSd{d in D,p in P[d]};/*虚拟网络在路径p上请求的频谱槽数量*/
param H{p in Path};/*路径p的跳数*/
param PNum{(u,v) in Ef};/*经过链路(u,v)的路径数量*/
param fs{d in D,p in Path,i in MSet};/*路径p上的第i个起始频谱槽索引*/
param MSlots;/*最大的频谱槽索引*/
param Sita{p in Path,(u,v) in Ef}, binary;/*二进制变量，*/
param Degree{u in Nv};/*虚拟节点的度*/

param Cost{u in Nv,v in Af[u]};/*运输代价 20190908增加*/
param CPU{u in Nv};/*运输代价 20190908增加*/

var y{d in D,p in Path}, binary;/*二进制变量，如果虚拟网络链路d映射在路径p上，则为1，否则为0*/
var M{u in Nv,v in Af[u]}, binary;/*二进制变量，*/
var E{d in D},integer,>=0;/*分配给虚拟网洛的频谱槽终点索引*/
var S{d in D},integer,>=0;/*分配给虚拟网洛的频谱槽起始节点索引*/
var B{d in D},integer,>=0;/*为虚拟网络分配的频谱槽总的数量*/
var x{d in D,p in Path,i in FS[d,p]}, binary;/*set FS{d in D,p in Path};路径p上可能的起始频谱槽索引集合*/
var Sigma{di in D,dj in DNo[di]}, binary;
var z{di in D,dj in DNo[di],p in Path}, integer;/*当di分配的slot小于dj分配的slot，E[di]=z[di,dj,p]*/
var s{d in D,(i,j) in Elink,a in MSet}, binary;
/*var zz{d in D,(i,j) in Elink,a in MSet}, binary;*/

set Af1{u in Nv};/*每个虚拟节点可映射的节点集合*/


/*minimize slotsMin: sum{d in D}sum{p in P[d]} NSd[d,p]*y[d,p]*(H[p]-2);*/

/*minimize slotsMin: sum{d in D}sum{p in P[d]} (NSd[d,p]*y[d,p]*(H[p]-2)+E[d]);*/
/*minimize slotsMin: sum{d in D} (E[d]) + sum{u in Nv,v in Af[u]}(Cost[u,v]*M[u,v]);   + sum{d in D}sum{p in P[d]} NSd[d,p]*y[d,p]*(H[p]-2)*0.999 */
minimize slotsMin: sum{u in Nv,v in Af1[u]}(Cost[u,v]*M[u,v]*CPU[u]);

/*minimize slotsMin: sum{u in Nv}sum{(u,v) in A[u]} M[u,v];*/

/**********start(节点和链路映射)*******************************************************************************/
s.t. NodeAssignmentCon1{u in Nv}: sum{(u,v) in A[u]} M[u,v]=1;
s.t. NodeAssignmentCon2{v in F}: sum{(u,v) in Afa[v]} M[u,v]<=1;
s.t. NodeAssignmentCon33{(u,v) in Ef}: sum{d in D,p in P[d]} Sita[p,u,v]*y[d,p] <= PNum[u,v]*M[u,v];
s.t. NodeAssignmentCon3{(u,v) in Ef}: sum{d in D,p in P[d]} Sita[p,u,v]*y[d,p] <= Degree[u]*M[u,v];/*每条链路的映射的总和大于2倍的节点映射M[u,v]*/
s.t. NodeAssignmentCon4{u in Nv}: sum{v in Af[u]} M[u,v]=1;

s.t. LinkMappingCon1{d in D}: sum{p in Path} y[d,p]=1;
s.t. LinkMappingCon2{p in Path}: sum{d in D} y[d,p]<=1;/*一条物理路径只能被映射盗一条虚拟链路*/
s.t. LinkMappingCon3{d in D}: sum{p in P[d]} y[d,p]=1;/*一条虚拟链路只能映射一条物理路径*/
s.t. LinkMappingCon4{u in Nv}: sum{d in D,p in P[d],(u,v) in Ef} Sita[p,u,v]*y[d,p]<=Degree[u];/*一条虚拟链路映射的一条物理路径的链路的总和为2条*/
/**********end***************************************************************************************************/

/**********start(频谱槽分配)*******************************************************************************/



s.t. OpticalRelatedCon1{d in D}: B[d]=sum{p in P[d]} y[d,p]*NSd[d,p];
s.t. OpticalRelatedCon2{d in D}: E[d]=S[d]+B[d]-1;
s.t. OpticalRelatedCon5{d in D}: E[d]<=MSlots;
s.t. OpticalRelatedCon3{d in D}: S[d]=sum{p in P[d]}sum{i in FS[d,p]}fs[d,p,i]*x[d,p,i];
s.t. OpticalRelatedCon4{d in D,p in P[d]}:sum{i in FS[d,p]}x[d,p,i]=y[d,p];

/*s.t. OpticalRelatedCon6{di in D,dj in DNo[di]}: Sigma[di,dj] + Sigma[dj,di] = 1;*/
s.t. OpticalRelatedCon7{di in D,dj in DNo[di],pa in P[di],pb in P[dj]}:E[dj]-S[di]<=(MSlots)*(Sigma[di,dj]+2-y[di,pa]-y[dj,pb]);

/*s.t. OpticalRelatedCon10{di in D,dj in DNo[di],p in Path}: z[di,dj,p]>=-1;*/
s.t. OpticalRelatedCon11{di in D,dj in DNo[di],p in P[di]}: z[di,dj,p]>=Sigma[di,dj]*(NSd[di,p])-1;/*当sigma==1时，NSd可能=1，则z将可以选择0*/
s.t. OpticalRelatedCon12{di in D,dj in DNo[di],p in Path}: z[di,dj,p]<=(S[dj]-1);
s.t. OpticalRelatedCon121{di in D,dj in DNo[di],p in Path}: z[di,dj,p]<=(E[dj]-1);
s.t. OpticalRelatedCon14{di in D,dj in DNo[di],p in Path}: z[di,dj,p]<=E[di];

/*s.t. OpticalRelatedCon15{(i,j) in Elink,a in MSet}: sum{d in D}s[d,i,j,a]<=1;*/
/*s.t. OpticalRelatedCon16{(i,j) in Elink,d in D}: sum{a in MSet}s[d,i,j,a]=B[d];*/
  
/*s.t. OpticalRelatedCon17{(i,j) in Elink,d1 in D,d2 in D}: sum{p in PathByLink[i,j],d in D}NSd[d,p]*y[d,p]>=E[d1]-E[d2];*/
/*s.t. OpticalRelatedCon18{(i,j) in Elink,d in D,a in MSet}: a*(s[d,i,j,a]-zz[d,i,j,a])<=S[d];*/
/**********end***************************************************************************************************/


end;