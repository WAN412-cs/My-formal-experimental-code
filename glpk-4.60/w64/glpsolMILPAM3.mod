/* ILP模型用于虚拟光网络的节点映射*/
        set Nv; /* 虚拟节点的集合 */
        set Ns; /* 物理节点的集合 */
        set X{i in Nv}; /* 每个虚拟节点可映射的物理节点集合 */

        param c{k in Ns}; /* 物理节点的计算单位代价 */
        param p{i in Nv}; /* 虚拟节点CPU */
        param U{k in Ns}; /* 物理节点k的剩余CPU */
        param sv{i in Nv,j in X[i]}; /* 虚拟节点CPU/物理节点影响力 */

        var A{i in Nv,k in Ns}, binary; /* 虚拟节点i映射到物理节点k上，0如果成功则为1，否则为0 */

        /* 选择目标函数（取消注释您想要使用的目标函数） */
        minimize nodeMappingCost:
        /*sum{i in Nv} sum{j in X[i]} (sv[i,j] * A[i,j]*p[i]);*/
        sum{i in Nv} sum{j in X[i]} (sv[i,j] * A[i,j]);
        /* 约束条件 */
        s.t. NodeAssignmentCon1{i in Nv}: sum{k in X[i]} A[i,k] = 1; /* 每个虚拟节点必须映射到一个物理节点 */
        s.t. NodeAssignmentCon2{k in Ns}: sum{i in Nv} A[i,k] <= 1; /* 每个物理节点最多映射一个虚拟节点 */
        s.t. NetResAssignmentCon1{k in Ns}: sum{i in Nv} p[i] * A[i,k] <= U[k]; /* 资源约束 */
        end;
