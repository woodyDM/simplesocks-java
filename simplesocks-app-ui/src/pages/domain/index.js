
import React from 'react';
import { Tabs, Icon } from 'antd';
import DomainList from '../../components/DomainList.js';
import styles from './index.less';

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
                <span className={styles.title} >该列表的网站<strong>永远不会</strong>被代理</span>
                <DomainList ></DomainList>
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
                <span className={styles.title}>该列表的网站<strong>总会</strong>被代理</span>
                <DomainList ></DomainList>
                </TabPane>
            </Tabs>
        );
    }
}