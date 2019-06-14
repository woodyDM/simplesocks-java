

import React from 'react';

export default class Pie extends React.Component{

    constants={
        color_success : 'lightgreen',
        color_proxy : 'orange',
        color_fail : 'red',
        span_total : 10,
        span_left: 1,
        span_rect: 2,
        span_blank: 1,
        span_num: 6,
        span_right: 0,

        hspan_total:6,
        hspan_top:2,
        hspan_rect:2,
        hspan_bottom:2,
        title_pix:15,
        font_offsetY:-2,

    }

    constructor(props){
        super(props); 
    }
 

    draw=()=>{
        const canvas = document.getElementById(this.props.id);
        const{width,height} = this.props;
        const data = this.props.dataSet;
        let hasData = false;
        if(data){
            const total = data.directNumber+data.proxyNumber+data.failedNumber;
            if(total>0){
                hasData = true; 
            }
        }
        if (canvas && canvas.getContext && data) {
            var ctx = canvas.getContext("2d");
            ctx.fillStyle='white';
            ctx.clearRect(0,0,width,height);
            ctx.fillStyle='black';
            if(hasData){
                
                const offsetY = 50;
                const x0 = width/2;
                const y0 = (height - offsetY)/2 + offsetY;
                const r = width/2-10;
                
                this.drawCircle(ctx, x0 , y0, r, data.directNumber, data.proxyNumber,data.failedNumber);
                this.drawTitle(ctx, width, offsetY, data.directNumber, data.proxyNumber,data.failedNumber);
            }else{
                ctx.fillStyle='black';
                ctx.font="20px Arial";
                ctx.fillText("暂时没有统计数据呢",10,50);
            }
            
        }
    }

    drawTitle = (ctx, width,height,success,proxy,fail)=>{
       
        this.drawOneTitle(ctx,0,"成功 "+ success,this.constants.color_success,width/3,height);
        this.drawOneTitle(ctx,1,"代理 "+ proxy,this.constants.color_proxy,width/3,height);
        this.drawOneTitle(ctx,2,"失败 "+ fail,this.constants.color_fail,width/3,height);
        
    }

    drawOneTitle = (ctx, offset, text,  color, width, height)=>{
        const x0 = offset * width;
        const rx0 = x0 + width * this.constants.span_left /this.constants.span_total;
        const ry0 = height * this.constants.hspan_top / this.constants.hspan_total;
        const rwidth =   width * ( this.constants.span_rect)/this.constants.span_total;
        const rheight = height * ( this.constants.hspan_rect)/this.constants.hspan_total;
        ctx.fillStyle= color;
        ctx.fillRect(rx0, ry0, rwidth, rheight);
        const fx0 = x0 + width * (this.constants.span_left+this.constants.span_rect+this.constants.span_blank)/this.constants.span_total;
        const fybase = height *(this.constants.hspan_top+this.constants.hspan_rect)/this.constants.hspan_total+this.constants.font_offsetY;
        ctx.font=this.constants.title_pix+"px Arial";
        ctx.fillText(text, fx0, fybase);


    }

    drawCircle=(ctx,x0,y0, r,success,proxy,fail)=>{
        const total = success + proxy + fail;
        if(success!==0){
            this.drawPie(ctx,x0,y0,r, 0, this.getAngle(success,total),   this.constants.color_success);
        }
        if(proxy!==0){
            this.drawPie(ctx,x0,y0,r, this.getAngle(success,total), this.getAngle(proxy+success,total),  this.constants.color_proxy);
        }
        if(fail!==0){
            this.drawPie(ctx,x0,y0,r, this.getAngle(proxy+success,total),Math.PI*2, this.constants.color_fail);
        }
    }

    drawPie = (ctx,x0,y0,r,start,end,color)=>{
        ctx.beginPath();
        ctx.fillStyle= color;
        ctx.arc(x0,y0, r, start, end ,false);
        ctx.lineTo(x0,y0);
        ctx.closePath();
        ctx.fill();
    }

    getAngle=(one,total)=>{
        return one/total*Math.PI*2;
    }

    render(){
        const width = (this.props.width?this.props.width:400);
        const height = (this.props.height?this.props.height:450);
        this.draw();
        return(
            <div>
                <canvas id={this.props.id} width={width} height={height}>
                    您的浏览器不支持canvas，请更换浏览器.
                </canvas>
            </div>);
         
    }

}