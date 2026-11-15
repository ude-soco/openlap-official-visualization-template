(function () {
  "use strict";
  const namespace = "http://www.w3.org/2000/svg";
  const observers = new WeakMap();
  function node(tag, attributes, text) {
    const element = document.createElementNS(namespace, tag);
    Object.entries(attributes || {}).forEach(([key, value]) => element.setAttribute(key, String(value)));
    if (text !== undefined) element.textContent = String(text);
    return element;
  }
  function draw(container, points, options, kind, width) {
    if (!container || !Array.isArray(points)) throw new Error("A chart container and points are required.");
    if (!points.every(point => typeof point.label === "string" && Number.isFinite(point.value))) {
      throw new Error("Every chart point needs a label and finite numeric value.");
    }
    const height = options.height || 420;
    const margin = { top: options.chartTitle ? 64 : 30, right: 24, bottom: 104, left: 64 };
    const plotWidth = width - margin.left - margin.right;
    const plotHeight = height - margin.top - margin.bottom;
    const minimum = Math.min(0, ...points.map(point => point.value));
    const maximum = Math.max(0, ...points.map(point => point.value));
    const range = maximum - minimum || 1;
    const y = value => margin.top + plotHeight * (maximum - value) / range;
    const spacing = plotWidth / Math.max(points.length, 1);
    const x = index => margin.left + spacing * (index + 0.5);
    const svg = node("svg", { viewBox: `0 0 ${width} ${height}`, role: "img", "aria-label": options.chartTitle || `${kind} chart` });
    svg.appendChild(node("title", {}, options.chartTitle || `${kind} chart`));
    svg.appendChild(node("desc", {}, points.map(point => `${point.label}: ${point.value}`).join("; ")));
    if (options.chartTitle) {
      const limit = Math.max(12, Math.floor((width - 32) / 11));
      const title = options.chartTitle.length > limit ? `${options.chartTitle.slice(0, limit - 1)}…` : options.chartTitle;
      svg.appendChild(node("text", { x: width / 2, y: 28, "text-anchor": "middle", class: "chart-title" }, title));
    }
    if (!points.length) svg.appendChild(node("text", { x: width / 2, y: height / 2, "text-anchor": "middle" }, "No data to display"));
    if (options.showGrid) {
      for (let index = 0; index <= 4; index++) {
        const value = minimum + range * index / 4;
        svg.appendChild(node("line", { x1: margin.left, x2: width - margin.right, y1: y(value), y2: y(value), class: "chart-grid" }));
        svg.appendChild(node("text", { x: margin.left - 10, y: y(value) + 4, "text-anchor": "end", class: "axis-label" }, Number(value.toPrecision(4))));
      }
    }
    svg.appendChild(node("line", { x1: margin.left, x2: width - margin.right, y1: y(0), y2: y(0), class: "chart-axis" }));
    if (kind === "line") svg.appendChild(node("polyline", {
      points: points.map((point, index) => `${x(index)},${y(point.value)}`).join(" "),
      fill: "none", stroke: options.seriesColor, "stroke-width": 3,
    }));
    points.forEach((point, index) => {
      const barWidth = Math.min(52, spacing * 0.7);
      const mark = kind === "line" ? node("circle", { cx: x(index), cy: y(point.value), r: 5, fill: options.seriesColor })
        : node("rect", { x: x(index) - barWidth / 2, y: Math.min(y(0), y(point.value)),
          width: barWidth, height: Math.abs(y(point.value) - y(0)), fill: options.seriesColor });
      mark.setAttribute("tabindex", "0");
      mark.setAttribute("class", "chart-mark");
      mark.setAttribute("aria-label", `${point.label}: ${point.value}`);
      mark.appendChild(node("title", {}, `${point.label}: ${point.value}`));
      svg.appendChild(mark);
      const labelLimit = width < 500 ? 18 : 28;
      const label = point.label.length > labelLimit ? `${point.label.slice(0, labelLimit - 1)}…` : point.label;
      const category = node("text", { transform: `translate(${x(index)},${height - margin.bottom + 20}) rotate(-35)`, "text-anchor": "end", class: "axis-label" }, label);
      category.appendChild(node("title", {}, point.label));
      svg.appendChild(category);
      if (options.showValues) svg.appendChild(node("text", {
        x: x(index), y: y(point.value) + (options.valueLabelPosition === "below" ? 20 : -12),
        "text-anchor": "middle", class: "value-label",
      }, point.value));
    });
    container.replaceChildren(svg);
  }
  function destroy(container) {
    observers.get(container)?.disconnect();
    observers.delete(container);
  }
  function render(container, points, options, kind) {
    destroy(container);
    let width = Math.max(320, container?.clientWidth || 720);
    draw(container, points, options, kind, width);
    if (typeof ResizeObserver !== "undefined") {
      let frame;
      const observer = new ResizeObserver(() => {
        const nextWidth = Math.max(320, container.clientWidth || 720);
        if (Math.abs(nextWidth - width) < 1) return;
        width = nextWidth;
        cancelAnimationFrame(frame);
        frame = requestAnimationFrame(() => draw(container, points, options, kind, width));
      });
      observer.observe(container);
      observers.set(container, { disconnect() {
        observer.disconnect();
        cancelAnimationFrame(frame);
      } });
    }
  }
  window.OpenLAPExample = Object.freeze({ version: "1.0.0", render, destroy });
}());
