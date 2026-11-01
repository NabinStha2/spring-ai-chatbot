const response = pm.response.json();

pm.visualizer.set(`
    <div style="padding: 20px; font-family: Arial;">
        <pre style="white-space: pre-wrap;">{{response}}</pre>
    </div>
`, {
    response: response.response
});
