
import React from 'react';
import { Tabs, Icon } from 'antd';
import DomainList from '../../components/DomainList.js';
import DomainTip from '../../components/DomainTip';
 

const { TabPane } = Tabs;


export default class DomainPage extends React.Component{
    render(){
        return (
            <Tabs defaultActiveKey="1">
                <TabPane
                tab={
                    <span>
                    <Icon type="home" />
                    白名单
                    </span>
                }
                key="1"
                >
                <DomainTip type='white'></DomainTip>
                <DomainList type='white'></DomainList>
                </TabPane>
                <TabPane
                tab={
                    <span>
                    <Icon type="safety" />
                    代理名单
                    </span>
                }
                key="2"
                >
                <DomainTip type='proxy'></DomainTip>
                <DomainList type='proxy'></DomainList>
                </TabPane>
            </Tabs>
        );
    }
}