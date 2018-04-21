/**
 * Templating function without eval() usage
 *
 * @param templateString
 * @param data
 * @returns {*}
 */
export function template(templateString, data) {
    if (typeof data === 'undefined') {
        return _.partial(template, templateString);
    } else {
        return templateString.replace(/\{\{([^}]+)}}/g, function(s, match) {
            var result = data;
            _.each(match.trim().split('.'), function(propertyName) {
                result = result[propertyName]
            });
            return _.escape(result);
        });
    }
}
