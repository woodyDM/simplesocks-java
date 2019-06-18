

import React from 'react';
import Pie from '../../components/Pie';
import ajax from '../../utils/request';
import {Card,Button} from 'antd';
import doge from '../../assets/doge.jpg';

export default class StatisticPage extends React.Component{

    state={
        tabKey : 'tab1',
    }

    componentDidMount(){
        this.refreshData();
    }

    reset=()=>{
        ajax.postEx(ajax.api.reset).then(res=>{
            this.refreshData();
        });
    }

    refreshData=()=>{
        ajax.getEx(ajax.api.statistic).then(res=>{
            this.setState({sData:res.data,tabKey:'tab1'});
        });
    }

    tabList = [
        {
            key: 'tab1',
            tab: '连接次数',
        },
        {
            key: 'tab2',
            tab: '流量统计',
        },
    ];

    onTabChange = (key)=>{

        this.setState({tabKey:key},()=>{
            if(key=='tab1'){
                this.refreshData();
            }
        });
    }

    render(){
        const ac = this.state.tabKey;
        const nowActive = (ac ? ac:'tab1');
        let startTime = "";
        if(this.state.sData){
            startTime = this.state.sData.startTime.replace("T"," ").substring(0,"2019-06-13 16:27:35".length)
        }
        const tabs = {
            'tab1':(<div>
                        <span style={{margin:'10px 20px'}}>统计开始时间：{startTime}</span>
                        <Button type='primary' onClick={this.reset}>重置数据</Button>
                        <Pie id = 'pie' width={400} height={500} dataSet={this.state.sData} />
                    </div>),
            'tab2':(<div>
                <div style={{fontSize:'18px'}}>为什么没有这个功能？别问，问就是太懒了。</div>
                <div style={{margin:"10px"}}><img src={doge} /></div>
            </div>),
        }
        return (
            <div>
                <Card
                style={{ width: '100%' }}
                title="数据统计"
                tabList={this.tabList}
                activeTabKey={nowActive}
                onTabChange={this.onTabChange}
                >
                    {tabs[nowActive]}
                </Card>
            </div>
                
         
            
        );
    }
}