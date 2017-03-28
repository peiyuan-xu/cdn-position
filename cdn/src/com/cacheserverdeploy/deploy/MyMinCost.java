package com.cacheserverdeploy.deploy;

import java.lang.reflect.Array;
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
         * next: 目前没有用到
         * capacity: 这条边的容量
         */
        int to, flow, cost, next, capatity,from;
        int real;
        boolean toVisited=false;

        public Edge(int from,int to, int capatity, int cost, int next,int real) {
            this.flow = 0;
            this.to = to;
            this.capatity = capatity;
            this.cost = cost;
            this.next = next;
            this.real=real;
            this.from=from;
        }
    }

    public static class Graph {
        int totalNodeNum, totalSourceNodeNum, totalConsumerNodeNum, serverCost, gEdgeCount;
        List<Edge>[] edgeArrList = null;
        // int[] gHead;

        // 图的最大流
        int maxFlow;
        //图的最小费用
        int minCost=0;

        public Graph(int totalNodeNum, int totalSourceNodeNum, int totalConsumerNodeNum, int serverCost) {
            this.totalNodeNum = totalNodeNum;
            this.totalSourceNodeNum = totalSourceNodeNum;
            this.totalConsumerNodeNum = totalConsumerNodeNum;
            this.serverCost = serverCost;

            this.edgeArrList = new List[totalNodeNum];
            for (int i = 0; i < totalNodeNum; i++) {
                edgeArrList[i] = new ArrayList<Edge>();
            }
            // this.gHead = new int[totalNodeNum];
        }

        public void addEdge(int from, int to, int capatity, int cost) {
            if (edgeArrList == null) {
                return;
            }

            edgeArrList[from].add(new Edge(from, to, capatity, cost, edgeArrList[to].size(),capatity));
            // gHead[from] = gEdgeCount++;

            // 增加反向边
            edgeArrList[to].add(new Edge(to, from, 0, -cost, edgeArrList[from].size(),0));
            // gHead[to] = gEdgeCount++;
        }
    }

    public Boolean Spfa(Graph graph, int start, int end) {
        if (graph == null || graph.edgeArrList == null) {
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
//        gVisited[start] = true;
        Arrays.fill(gVisited, false);

        // nodeVisitedNum代表点进入过几次队列
        int[] nodeVisitedNum = new int[graph.totalNodeNum];

        // currentFlow代表最多路算到该点时，该点的最大流
        int[] currentFlow = new int[graph.totalNodeNum];
        Arrays.fill(currentFlow, Integer.MAX_VALUE);


        Queue<Integer> Q = new PriorityQueue<>();
        Q.add(start);

        while (!Q.isEmpty()) {
            int tempStart = Q.remove();
            gVisited[tempStart] = false;

            // foreach 顶点start的邻接边
            for (int i = 0; i < graph.edgeArrList[tempStart].size(); i++) {
                Edge aEdge = graph.edgeArrList[tempStart].get(i);

                /* 过滤flow==capatity，相当于之前权重无限大
                if(aEdge.capatity <= aEdge.flow){
                    continue;
                }*/

                int tempNext = aEdge.to;

                // 计算路径增加费用，当该边是从源点开始时要进行特殊处理
                int tempCost = aEdge.cost;

                // aEdge.capatity > 0 过滤掉逆向边没有可回退流量的情况
                if (aEdge.capatity > 0 && gDist[tempStart] + tempCost < gDist[tempNext]) {
                    gDist[tempNext] = gDist[tempStart] + tempCost;
                    gPreNode[tempNext] = tempStart;
                    gPreEdge[tempNext] = i;

                    // 更新路径到该点的流量值
                    currentFlow[tempNext] = Math.min(currentFlow[tempStart], aEdge.capatity);

                    // 松弛操作后，如果点tempNext没有在Queue中将点放入
                    if (!gVisited[tempNext]) {
                        gVisited[tempNext] = true;
                        Q.add(tempNext);

                        // 入队次数++
                        nodeVisitedNum[tempNext]++;

                        if (nodeVisitedNum[tempNext] >= graph.totalNodeNum) {
                            System.out.println("存在负环");
                            return false;
                        }
                    }
                }
            }
        }
        if (gPreNode[end] == -1) {
            // 没有到达终点
            return false;
        }
        //更新边的流量
        updateFlowOfEdge(graph, currentFlow[end], gPreNode, gPreEdge, start, end);
        return true;
    }

    /*
     *　跑完最短路得到路径后，更新边的流量
     *  @maxFlowForEnd: currentFlow[end]的终点的流量
     */
    public void updateFlowOfEdge(Graph graph, int maxFlowForEnd, int[] gPreNode, int[] gPreEdge, int start, int end) {

        int df = maxFlowForEnd;
//        minCost=0;

        // 更新图的最大流
        graph.maxFlow += maxFlowForEnd;


        // 更新每条边的flow和最小费用
        for (int temp = end; temp != start; temp = gPreNode[temp]) {
            Edge edge = graph.edgeArrList[gPreNode[temp]].get(gPreEdge[temp]);
            Edge edgeBack = findBackEdge(graph, edge, gPreNode[temp]);

            if(edge.cost>=0){//正向边
                edge.flow += df;
                edge.capatity -= df;
                edgeBack.capatity += df;
            }else{
                edgeBack.flow -= df;
                edgeBack.capatity += df;
                edge.capatity -= df;
            }
//            // 更新正向边容量
//            edge.flow += df;
//            edge.capatity -= df;
//
//            // 更新反向边容量
//            edgeBack.capatity += df;
//            edgeBack.flow -= df;

//            minCost += edge.cost;
        }

    }

    /*
     * 给定一条边，求该边对应的反向边
     */
    public Edge findBackEdge(Graph graph, Edge edge, int preNode) {
        Edge edgeBack = null;

        List<Edge> edgeList = graph.edgeArrList[edge.to];
        int flag = 1, i = 0;
        while (flag == 1) {
            if (edgeList.get(i++).to == preNode) {
                edgeBack = edgeList.get(--i);
                flag = 0;
                break;
            }
        }
        return edgeBack;
    }

    /**
     * 输出路径信息
     */
    LinkedList<String> pathList=new LinkedList<String>();
    List<Integer> pathCostList=new ArrayList<Integer>();

    public String[] getRes(Graph graph,int start,int end){
        int size=graph.edgeArrList.length;
        List<MyMinCost.Edge> list=null;

        for(MyMinCost.Edge edge:graph.edgeArrList[start]){
            while(edge.flow!=0){//有流量下走，总能走到end
                StringBuilder sb=new StringBuilder();

                int minFlow=edge.flow;
                int tempCost=0;//记录总的单位费用

                boolean[] visited=new boolean[size];
                Arrays.fill(visited,false);
                list=new ArrayList<>();
                list.add(edge);

                visited[start]=true;
                visited[edge.to]=true;
                sb.append(edge.to+" ");
                MyMinCost.Edge tpEdge =edge;
                while (tpEdge.to!=end) {
                    int to=tpEdge.to;
                    int i = 0;
                    tpEdge = graph.edgeArrList[to].get(i);
                    while (tpEdge.flow == 0||visited[tpEdge.to]) {
                        tpEdge = graph.edgeArrList[to].get(++i);
                    }
                    visited[tpEdge.to]=true;
                    list.add(tpEdge);
                    if(tpEdge.to<start) {//添加边
                        sb.append(tpEdge.to + " ");
                    }
                    minFlow=Math.min(minFlow,tpEdge.flow);
                    tempCost+=tpEdge.cost;
                }
                //计算这条路径费用并加入总费用
                graph.minCost+=tempCost*minFlow;
                //削减流量
                for(MyMinCost.Edge edgeTemp:list){
                    edgeTemp.flow-=minFlow;
                }
                sb.append(Deploy.node_consumer.get(tpEdge.from)+" "+minFlow);//加入消费节点并连接流量
                pathList.add(sb.toString());
            }
        }
        int pathNum=pathList.size();
        pathList.addFirst("");
        pathList.addFirst(pathNum+"");
        return pathList.toArray(new String[pathList.size()]);
    }
}
