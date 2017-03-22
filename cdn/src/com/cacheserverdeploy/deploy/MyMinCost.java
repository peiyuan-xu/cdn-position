package com.cacheserverdeploy.deploy;

import java.util.*;

/**
 * Author: Wucheng
 * Date: 2017/3/20 13:15
 * Abstract:
 */
public class MyMinCost {
    /*
     * 最小费用最大流，使用spfa
     */
    public final static int MAX_NODE = 7280;

    static class Edge {
        /*
         * to: 这条边要到的顶点
         * flow: 这条边上的流量
         * cost: 这条边的单位租价
         * netxt: 目前没有用到
         * capacity: 这条边的容量
         */
        int to, flow, cost, next, capatity;

        public Edge(int to, int capatity, int cost, int next) {
            this.flow = 0;
            this.to = to;
            this.capatity = capatity;
            this.cost = cost;
            this.next = next;
        }
    }

    static class Graph{
        int totalNodeNum, totalSourceNodeNum, totalConsumerNodeNum, serverCost, gEdgeCount;
        List<Edge>[] edgeArrList = null;
        int[] gHead;

        // the max flow of this graph
        int maxFlow;

        public Graph(int totalNodeNum, int totalSourceNodeNum, int totalConsumerNodeNum, int serverCost) {
            this.totalNodeNum = totalNodeNum;
            this.totalSourceNodeNum = totalSourceNodeNum;
            this.totalConsumerNodeNum = totalConsumerNodeNum;
            this.serverCost = serverCost;

            this.edgeArrList = new List[totalNodeNum];
            for(int i = 0; i< totalNodeNum; i++){
                edgeArrList[i] = new ArrayList<Edge>();
            }
            this.gHead = new int[totalNodeNum];
        }

        public  void addEdge(int from, int to, int capatity, int cost){
            if(edgeArrList == null){
                return;
            }

            edgeArrList[from].add(new Edge(to, capatity, cost, edgeArrList[to].size()));
            // gHead[from] = gEdgeCount++;

            // 增加反向边
            edgeArrList[to].add(new Edge(from, 0, -cost, edgeArrList[from].size()));
            // gHead[to] = gEdgeCount++;
        }
    }

    Boolean Spfa(Graph graph, int start, int end){
        if(graph == null || graph.edgeArrList == null){
            return false;
        }

        // gDist代表S到I点的当前最短距离
        int[] gDist = new int[graph.totalNodeNum];
        Arrays.fill(gDist, Integer.MAX_VALUE);
        gDist[start] = 0;

        // gPreNode代表S到I的当前最短路径中I点之前的一个点的编号
        int[] gPreNode = new int[graph.totalNodeNum];
        Arrays.fill(gPreNode, -1);

        // gPreEdge代表最短路下，该点的前置边
        int[] gPreEdge = new int[graph.totalNodeNum];

        // gVisited代表每个点是否处在队列中
        Boolean[] gVisited = new Boolean[graph.totalNodeNum];
        gVisited[start] = true;

        // nodeVisitedNum代表点进入过几次队列
        int[] nodeVisitedNum = new int[graph.totalNodeNum];

        // currentFlow代表最多路算到该点时，该点的最大流
        int[] currentFlow = new int[graph.totalNodeNum];
        Arrays.fill(currentFlow, Integer.MAX_VALUE);


        Queue<Integer> Q = new PriorityQueue<>();
        Q.add(start);

        while (!Q.isEmpty()){
            int tempStart = Q.remove();
            gVisited[tempStart] = false;

            // foreach 顶点start的邻接边
            for(int i = 0; i < graph.edgeArrList[tempStart].size(); i++){
                Edge aEdge = graph.edgeArrList[tempStart].get(i);

                /* 过滤flow==capatity，相当于之前权重无限大
                if(aEdge.capatity <= aEdge.flow){
                    continue;
                }*/

                int tempNext = aEdge.to;

                // 计算路径增价费用，当该边是从源点开始时要进行特殊处理
                int tempCost = caculateCostOfEdge(aEdge);

                // aEdge.capatity > 0 过滤掉逆向边没有可回退流量的情况
                if(aEdge.capatity > 0 && gDist[tempStart] + tempCost < gDist[tempNext]){
                    gDist[tempNext] = gDist[tempStart] + tempCost;
                    gPreNode[tempNext] = tempStart;
                    gPreEdge[tempNext] = i;

                    // 更新路径到该点的流量值
                    currentFlow[tempNext] = Math.min(currentFlow[tempStart], aEdge.capatity - aEdge.flow);

                    // 松弛操作后，如果点tempNext没有在Queue中将点放入
                    if(!gVisited[tempNext]){
                        gVisited[tempNext] = true;
                        Q.add(tempNext);

                        // 入队次数++
                        nodeVisitedNum[tempNext]++;

                        if(nodeVisitedNum[tempNext] >= graph.totalNodeNum){
                            System.out.println("存在负环");
                            return false;
                        }
                    }
                }

            }
        }

        if(gPreNode[end] == -1){
            // 没有到达终点
            return false;
        }

        return true;
    }


    /*
     * 计算路过一天边的代价，特别是源点出发的路径
     * 这类路径第一次经过算服务器价格，之后再经过算0
     */

    public int caculateCostOfEdge(Edge aEdge){


        return 10;
    }

    /*
     *　跑完最短路得到路径后，更新边的流量
     *  @maxFlowForEnd: currentFlow[end]的终点的流量
     */
    public void updateFlowOfEdge(Graph graph, int maxFlowForEnd, int[] gPreNode, int[] gPreEdge, int start, int end){

        int df =maxFlowForEnd;

        // 更新图的最大流
        graph.maxFlow += maxFlowForEnd;


        // 更新每条边的flow和最小费用
        for(int temp = end; temp != start; temp = gPreNode[temp]){
            Edge edge = graph.edgeArrList[gPreNode[temp]].get(gPreEdge[temp]);

            // 更新正向边容量
            edge.flow += df;
            edge.capatity -= df;

            // 更新反向边容量
            Edge edgeBack = findBackEdge(graph, edge);
            edgeBack.capatity += df;

            int tempCost = caculateCostOfEdge(edge);
        }

    }

    /*
     * 给定一条边，求该边对应的反向边
     */
    public Edge findBackEdge(Graph graph, Edge edge){
        Edge edgeBack = null;

        return edgeBack;
    }

}
