electron视频渲染示例:

```js
// 由客户端触发`frame`事件
on('frame', ({uid: number, data: {data: Array, width, height}}) => {
  const render = getRender(uid); 
  render.update(data);
})

// 对即构提供的webglRender封装
class Render {
  public id: number; // 唯一标识
  public uid: number; 
  public render: any;
  private node: undefined | HTMLCanvasElement;
  private parent: undefined | HTMLElement;

  constructor(uid: number) {
    this.uid = uid;
    this.render = new window.zegoWebglRender(); // 即构提供
    const canvas = document.createElement('canvas');
    this.node = canvas;
    this.render.initGLfromCanvas(canvas);
    this.id = 1;
  }

  public attach(parentNode: HTMLDivElement) {
    this.parent = parentNode;
    parentNode.appendChild(canvas);
  }

  public release() {
    if (this.parent && this.node) {
      this.clear();
      this.parent.removeChild(this.node);
      this.parent = undefined;
    }
  }

  public update = (data: {data:Array, width: number, height: number}) => {
    this.render.draw(data.data, data.width, data.height);
  };

  public getCanvas = () => {
    return this.node;
  }

  public clear = () => {
    try {
      if (!this.node) return;
      const gl = this.node.getContext('webgl');
      if (gl) {
        gl.clearColor(0.0, 0.0, 0.0, 1.0);
        gl.clear(gl.COLOR_BUFFER_BIT);
      }
    } catch (ex) {
      
    }
  };
}

// 外部调用接口
client.getRenderer(uid: number): Render

client.releaseRenderer(id: number)
```