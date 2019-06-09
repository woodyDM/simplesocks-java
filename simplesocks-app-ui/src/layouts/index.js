import styles from './index.less';
import {Layout, Menu, Icon} from 'antd';
import router from 'umi/router';
const {Header, Content,Footer,Sider} = Layout;


function handleClick (item,key,keypath){
  const path = item.key;
  router.push(path);
}


function BasicLayout(props) {
  return (
    <Layout>
    <Header>
       
      <Menu
      theme="dark"
      mode="horizontal"
      defaultSelectedKeys={['2']}
      style={{ lineHeight: '64px' }}>
      
        <Menu.Item className={styles.headerRight} key="1">Github</Menu.Item>
        <Menu.Item className={styles.headerRight}key="2">Github2</Menu.Item>
        <Menu.Item className={styles.headerRight}key="3">Github3</Menu.Item>
        
      
    </Menu>
      
      
    </Header>
    <Content style={{ padding: '0 0px' }}>
     
      <Layout style={{ padding: '24px 0', background: '#fff' }}>
        <Sider width={300} style={{ background: '#fff' }}>
          <Menu
            mode="vertical"
            defaultSelectedKeys={['1']}
            defaultOpenKeys={['1']}
            style={{ height: '100%'  }}
            onClick = {handleClick.bind(this)}
          >
            <Menu.Item key='/'> 
                <Icon className={styles.menuIcon} type="desktop" />
                <span>
                  概况
                </span>
            </Menu.Item>
            <Menu.Item key='/setting'> 
                <Icon className={styles.icon} type="edit" />
                <span>
                  一般配置
                </span>
            </Menu.Item>
            <Menu.Item key='/domain'> 
                <Icon className={styles.icon} type="global" />
                <span>
                域名配置
                </span>
            </Menu.Item>
            <Menu.Item key='/statistic'> 
                <Icon className={styles.icon} type="pie-chart" />
                <span>
                数据统计
                </span>
            </Menu.Item>
            <Menu.Item key='/git'> 
                <Icon className={styles.icon} type="gitlab" />
                <span>
                Git代理
                </span>
            </Menu.Item>
          </Menu>
        </Sider>
        <Content style={{ padding: '0 24px', minHeight: 580 }}>{props.children}</Content>
      </Layout>
    </Content>
    <Footer style={{ textAlign: 'center'  }}><a href='https://github.com/woodyDM/simplesocks-java'>SimpleSocks</a> ©2019 </Footer>
  </Layout>
  );
}

export default BasicLayout;
