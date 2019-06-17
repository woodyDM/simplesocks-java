import React from 'react';
import styles from './index.less';
import {Tooltip,Icon} from 'antd';

export default class DomainTip extends React.Component{

    render(){
        const msg = (this.props.type==='white'?"永远不会":"总会")
        return (
            <div>
                <span className={styles.title} >该列表的域名<strong>{msg}</strong>被代理</span>
                <span className={styles.tip}>
                    <Tooltip title="如果欲访问的域名内，包含列表中任意一个字符串，则规则生效。建议新增一级域名">
                        <Icon type="question-circle-o" size='large' />
                    </Tooltip>
                </span>
            </div>
        );
    }
}